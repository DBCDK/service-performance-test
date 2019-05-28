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
package dk.dbc.service.performance;

import java.io.IOException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper of input to a String stream
 * <p>
 * Each event (usually line) is supplied as a stream, for filtering/collection
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public abstract class LineSource implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(LineSource.class);

    /**
     * Fetch next line
     *
     * @return line or null if source is drained
     * @throws IOException In case of an error
     */
    protected abstract String nextLine() throws IOException;

    @Override
    public abstract void close();

    public Stream<String> stream() {
        return StreamSupport.stream(new Spliterator<String>() {
            @Override
            public boolean tryAdvance(Consumer<? super String> cnsmr) {
                try {
                    String text = nextLine();
                    if (text == null)
                        return false;
                    cnsmr.accept(text);
                    return true;
                } catch (IOException ex) {
                    log.error("Cannot get log line from source: {}", ex.getMessage());
                    log.debug("Cannot get log line from source: ", ex);
                    return false;
                }
            }

            @Override
            public Spliterator<String> trySplit() {
                throw new UnsupportedOperationException("Doesn't support parallel");
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return IMMUTABLE | ORDERED;
            }
        }, false);
    }
}
