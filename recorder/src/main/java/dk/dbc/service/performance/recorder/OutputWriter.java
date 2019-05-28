/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of performance-test-recorder
 *
 * performance-test-recorder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * performance-test-recorder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.service.performance.recorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Consumer of {@link LogLine}s, that counts then and puts them on a
 * {@link OutputStream}
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class OutputWriter implements AutoCloseable, Consumer<LogLine> {

    private static final Logger log = LoggerFactory.getLogger(OutputWriter.class);

    private final SortedSet<Entry> entries;
    private final OutputStream os;
    private final int orderBufferSize;
    private long duration;
    private final long limit;
    private final BiConsumer<OutputStream, LogLine> firstLineMetadata;

    private Instant origin;
    private long lastEntryTimeOffset;
    private Long timeFirstDelta;
    private long count;
    private boolean completed;

    /**
     * Construct a stream consumer
     *
     * @param os                Stream to put lines onto
     * @param orderBufferSize   how many lines should be buffered to mitigate
     *                          kafka out of order lines
     * @param duration          how many ms to run for
     * @param limit             how many lines to acquire
     * @param firstLineMetadata method that takes an output stream, and the
     *                          first log line. To create headers in the output.
     */
    public OutputWriter(OutputStream os, int orderBufferSize, long duration, long limit, BiConsumer<OutputStream, LogLine> firstLineMetadata) {
        this.entries = new TreeSet<>();
        this.lastEntryTimeOffset = 0L;
        this.timeFirstDelta = null;
        this.os = os;
        this.orderBufferSize = orderBufferSize;
        this.duration = duration;
        this.limit = limit;
        this.origin = null;
        this.firstLineMetadata = firstLineMetadata;
        this.count = 0;
        this.completed = false;
        log.debug("orderBufferSize = {}", orderBufferSize);
        log.debug("duration = {}", duration);
        log.debug("limit = {}", limit);
    }

    @Override
    public void close() {
        try {
            // If not completed, but source was drained output from cache
            log.debug("completed = {}", completed);
            if (!completed)
                entries.forEach(this::outputEntry);
        } catch (CompletedException ex) {
            log.debug("Reached limit during shutdown");
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                log.error("Error closing output: {}", ex.getMessage());
                log.debug("Error closing output: ", ex);
            }
        }
    }

    /**
     * Stash a log line in the cache, when cache reaches it's limit, output
     * oldest log line.
     *
     * @param logLine log line
     */
    @Override
    public void accept(LogLine logLine) {
        if (origin == null)
            origin = logLine.getInstant();
        long currentOffset = logLine.timeOffsetMS(origin);
        entries.add(new Entry(currentOffset, logLine));
        if (entries.size() > orderBufferSize) {
            Iterator<Entry> i = entries.iterator();

            Entry entry = i.next();
            i.remove();

            outputEntry(entry);
        }
    }

    /**
     * Dump an entry onto an output stream
     * <p>
     * Ensure order
     *
     * @param entry
     */
    private void outputEntry(Entry entry) {
        long entryTimeOffset = entry.getTimeOffset();
        if (timeFirstDelta == null) {
            lastEntryTimeOffset = timeFirstDelta = entryTimeOffset;
            duration += entryTimeOffset;
            log.debug("lastEntryTimeOffset = {}", lastEntryTimeOffset);
            log.debug("timeFirstDelta = {}", timeFirstDelta);
            log.debug("duration + timeFirstDelta = {}", duration + timeFirstDelta);
            firstLineMetadata.accept(os, entry.getLogLine());
        }
        if (entryTimeOffset >= duration + timeFirstDelta) {
            completed = true;
            throw new CompletedException();
        }
        if (entryTimeOffset - timeFirstDelta < lastEntryTimeOffset) {
            log.warn("Buffered output is out of order, increase buffer size? (outputted={}, next={})", lastEntryTimeOffset, entryTimeOffset);
        } else {
            lastEntryTimeOffset = entryTimeOffset;
            entry.outputTo(os, timeFirstDelta);
            if (++count >= limit) {
                this.completed = true;
                throw new CompletedException();
            }
        }
    }

    /**
     * Cache entry (timeOffset/content)
     */
    private static class Entry implements Comparable<Entry> {

        private final long timeOffset;
        private final LogLine logLine;

        private Entry(long timeOffset, LogLine logLine) {
            this.timeOffset = timeOffset;
            this.logLine = logLine;
        }

        private long getTimeOffset() {
            return timeOffset;
        }

        private LogLine getLogLine() {
            return logLine;
        }

        @Override
        public int compareTo(Entry t) {
            int ret = Long.compare(timeOffset, t.timeOffset);
            ret = ret != 0 ? ret : logLine.compareTo(t.logLine);
            return ret;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (int) ( this.timeOffset ^ ( this.timeOffset >>> 32 ) );
            hash = 41 * hash + Objects.hashCode(this.logLine);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Entry other = (Entry) obj;
            return this.timeOffset == other.timeOffset &&
                   Objects.equals(this.logLine, other.logLine);
        }

        private void outputTo(OutputStream os, long delta) {
            String line = new StringBuilder()
                    .append(timeOffset - delta)
                    .append(" ")
                    .append(logLine.getQuery())
                    .append("\n")
                    .toString();
            try {
                os.write(line.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
