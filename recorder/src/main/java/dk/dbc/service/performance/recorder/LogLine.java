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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * SolR log line abstraction
 * <p>
 * This has quite a log of hardcoded business logic
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public final class LogLine implements Comparable<LogLine> {

    private static final Logger log = LoggerFactory.getLogger(LogLine.class);

    private static final ObjectMapper O = new ObjectMapper();

    private static final String PERFTEST_FLAG = "dbcPerfTest=true";

    private final boolean valid;
    private final Instant instant;
    private final String app;
    private final String query;

    /**
     * Convert a log line into an object
     *
     * @param text log line
     * @return LogLine object
     */
    public static LogLine of(String text) {
        try {
            JsonNode obj = O.readTree(text);
            JsonNode timestamp = obj.get("timestamp");
            JsonNode app = obj.get("app");
            JsonNode message = obj.get("message");
            if (timestamp == null || app == null || message == null)
                return new LogLine(false, Instant.MIN, null, null);
            String query = queryOf(message.asText(""));
            if (query == null)
                return new LogLine(false, Instant.MIN, null, null);
            Instant instant = parseTimeStamp(timestamp.asText(""));

            return new LogLine(true, instant, app.asText(""), query);
        } catch (IOException ex) {
            log.debug("Error parsing JSON log line: ", ex);
            return new LogLine(false, Instant.MIN, null, null);
        }
    }

    private LogLine(boolean valid, Instant instant, String app, String query) {
        this.valid = valid;
        this.instant = instant;
        this.app = app;
        this.query = query;
    }

    /**
     * This object is a valid log line
     * <p>
     * It is invalid if it wasn't JSON, wasn't a select call, was a perf-test
     * replay request
     *
     * @return should we keep this
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Which application was listed in the JSON
     *
     * @return application name
     */
    public String getApp() {
        return app;
    }

    /**
     * When the line was logged
     *
     * @return timestamp
     */
    public Instant getInstant() {
        return instant;
    }

    /**
     * The actual query requested
     *
     * @return query-string
     */
    public String getQuery() {
        return query;
    }

    /**
     * Get age of log line relative to a timestamp
     *
     * @param origin when to compare to
     * @return milliseconds
     */
    public long timeOffsetMS(Instant origin) {
        return Duration.between(origin, instant).toMillis();
    }

    @Override
    public int compareTo(LogLine t) {
        return query.compareTo(t.query);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + ( this.valid ? 1 : 0 );
        hash = 89 * hash + Objects.hashCode(this.instant);
        hash = 89 * hash + Objects.hashCode(this.app);
        hash = 89 * hash + Objects.hashCode(this.query);
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
        final LogLine other = (LogLine) obj;
        return this.valid == other.valid &&
               Objects.equals(this.app, other.app) &&
               Objects.equals(this.query, other.query) &&
               Objects.equals(this.instant, other.instant);
    }

    @Override
    public String toString() {
        return "LogLine{" + "valid=" + valid + ", instant=" + instant + ", app=" + app + ", query=" + query + '}';
    }

    /**
     * Convert a timestamp to an instant
     *
     * @param text timestamp from log line
     * @return timestamp for when the line was logged
     */
    private static Instant parseTimeStamp(String text) {
        return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(text));
    }

    /**
     * Extract query
     * <p>
     * <ul>
     * <li>If path is not "/select", then return null
     * <li>If params is unset or empty, then return null
     * <li>If params is contain distrib=false, then return null (logging from a
     * distributed query)
     * <li>If params is contain PERFTEST_FLAG, then return null replayed query
     * (no feedback loop)
     * <p>
     * </ul>
     *
     * @param message from log
     * @return query string or null if not a valid query, with trackingId
     *         removed, and perftest-flag set
     */
    private static String queryOf(String message) {
        try {
            Map<String, String> parts = Arrays.stream(message.split("\\s+"))
                    .filter(s -> s.contains("="))
                    .map(s -> s.split("=", 2))
                    .collect(Collectors.toMap(a -> a[0], a -> a[1]));

            String path = parts.getOrDefault("path", "");
            if (path == null || !path.equals("/select"))
                return null;

            String params = parts.get("params");
            if (params == null || params.isEmpty())
                return null;

            String queryString = params.substring(1, params.length() - 1);

            String queryStringMatcher = "&" + queryString + "&";
            if (queryStringMatcher.contains("&distrib=false&") ||
                queryStringMatcher.contains("&" + PERFTEST_FLAG + "&"))
                return null;

            return ( queryString + "&" + PERFTEST_FLAG ).replaceFirst("&trackingId=[^&]*&", "&");
        } catch (RuntimeException e) {
            return null;
        }
    }
}
