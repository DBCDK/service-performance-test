package dk.dbc.service.performance.replayer;
/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of performance-test
 *
 * performance-test is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * performance-test is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * File created: 20/03/2019
 */

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collecor of status for program progression
 *
 * @author Mike Andersen (mran@dbc.dk)
 */
public class LogCollector {
    private List<LogEntry> log;
    private Map conf;
    private final ConcurrentMap<String, AtomicLong> counterMap ;
    private int statusCode;
    private String statusMessage;



    public LogCollector() {
        log =  new ArrayList<LogEntry>(100);
        conf = new HashMap();
        counterMap = new ConcurrentHashMap<>();
    }

    /**
     * Add the config map to the log
     * @param conf Map of configurations
     */
    public void addConfig(Map conf) { this.conf = conf; }

    /**
     * Add one status note to the log
     *
     * @param status The message
     */
    public void addStatusEntry(String status) {
        LogEntry e = new LogEntry();
        e.setStatus(status);
        this.log.add(e);
    }

    /**
     * Add the complete program run status
     * @param code The exitcode for the program
     * @param message A desciption of the exit-code
     */
    public void addRunStatus(int code, String message) {
        this.statusCode = code;
        this.statusMessage = message;
    }

    /**
     * Add a log entry to the log
     *
     * @param entry Logentry to be stored
     */
    public void addEntry(LogEntry entry) {
        log.add(entry);
    }


    /**
     * Increment the counter for httpResponse codes
     *
     * @param httpResponse The numrical (as string) http responsecode
     */
    public void incrementFor(String httpResponse) {
        counterMap.computeIfAbsent(httpResponse, p -> new AtomicLong()).incrementAndGet();
    }

    /**
     * Dump the log (as json) to the give OutputStream
     *
     * @param os Stream to otput to
     * @throws IOException if anything goes wrong during writing
     */
    public void dump(OutputStream os) throws IOException {
        if(os == null)
            return;

        Map status = new HashMap();
        status.put( "code", this.statusCode);
        status.put("message", this.statusMessage);

        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
        Map output = new HashMap();
        output.put("configuration", conf);
        output.put("loglines", log);
        output.put("statistics", counterMap);
        output.put("status", status);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(w, output);
    }


    public static LogEntry newEntry() {
        return new LogEntry();
    }

    public static class LogEntry {
        private long originalTimeDelta;
        private long callDelay;
        private long callDuration;
        private String query;
        private String status;
        private long timestamp;

        public LogEntry() {
            this.timestamp = System.currentTimeMillis();
            this.originalTimeDelta = 0;
            this.callDelay = 0;
            this.callDuration = 0;
            this.query = "";
            this.status = "";
        }

        public void setTimes(long originalDelay, long actualDelay) {
            this.originalTimeDelta = originalDelay;
            this.callDelay = actualDelay;
        }

        public void setCallDuration(long callDuration) {
            this.callDuration = callDuration;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "LogEntry{" +
                    "  timestamp=" + timestamp +
                    ", originalTimeDelta=" + originalTimeDelta +
                    ", callDelay=" + callDelay +
                    ", callDuration=" + callDuration +
                    ", query='" + query + "'" +
                    ", status='" + status + "'" +
                    '}';
        }

        public long getOriginalTimeDelta() {
            return originalTimeDelta;
        }

        public long getCallDelay() {
            return callDelay;
        }

        public long getCallDuration() {
            return callDuration;
        }

        public String getQuery() {
            return query;
        }

        public String getStatus() {
            return status;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
