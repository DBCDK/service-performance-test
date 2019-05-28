/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of performance-test-replayer
 *
 * performance-test-replayer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * performance-test-replayer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.service.performance.replayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Performance test log line abstraction
 *
 * @author Mike Andersen (mran@dbc.dk)
 */
public final class LogLine {

    private final Long timeDelta;
    private final String query;
    private final boolean isvalid;

    private static final Pattern COMMENT_PATTERN = Pattern.compile("#.*");
    private static final Pattern SPACE_PATTERN = Pattern.compile( "(\\d+)\\s+(.*)");

    /**
     * Convert a log line into an object
     *
     * @param text log line
     * @return LogLine object
     */
    public static LogLine of(String text) {
        if( COMMENT_PATTERN.matcher(text).matches() )
            return new LogLine(0l, "", false);

        Matcher m = SPACE_PATTERN.matcher(text);
        m.matches();
        return  new LogLine(Long.parseLong(m.group(1)), m.group(2), true);
    }

    private LogLine(Long timeDelta, String query, boolean isvalid ) {
        this.timeDelta = timeDelta;
        this.query = query;
        this.isvalid = isvalid;
    }

    /**
     * When the line was logged relative to start
     *
     * @return Long
     */
    public Long getTimeDelta() {
        return timeDelta;
    }

    /**
     * The query requested
     *
     * @return query-string
     */
    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "LogLine{" + "timeDelta=" + timeDelta + ", query=" + query + '}';
    }

    public boolean isValid() {
        return isvalid;
    }
}
