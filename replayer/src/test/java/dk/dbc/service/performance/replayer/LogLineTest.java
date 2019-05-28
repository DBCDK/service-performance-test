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
package dk.dbc.service.performance.replayer;

import dk.dbc.service.performance.LineSource;
import dk.dbc.service.performance.LinesInputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class LogLineTest {

    private static final String LINE_ONE   = "0 q=REDACTED&defType=edismax&fl=REDACTED&start=0&fq=REDACTED&rows=99999&wt=phps&dbcPerfTest=true";
    private static final String LINE_TWO   = "100 q=REDACTED&defType=edismax&fl=REDACTED&start=0&fq=REDACTED&rows=99999&wt=phps&dbcPerfTest=true";
    private static final String MULTI_LINE = "0 q=REDACTED&defType=edismax&fl=REDACTED&start=0&fq=REDACTED&rows=99999&wt=phps&dbcPerfTest=true\n" +
                                             "35 q=REDACTED&defType=edismax&fl=REDACTED&start=0&fq=REDACTED&rows=99999&wt=phps&dbcPerfTest=true\n" +
                                             "80 q=REDACTED&start=1&fq=REDACTED&fq=REDACTED&rows=0&wt=phps&dbcPerfTest=true";


    @Test(timeout = 2_000L)
    public void testSimple() throws Exception {
        System.out.println("testSimple");
        LogLine logLine = LogLine.of(LINE_ONE);
        assertThat(logLine.getTimeDelta(), is(greaterThanOrEqualTo(0L)));

        logLine = LogLine.of(LINE_TWO);
        assertThat(logLine.getTimeDelta(), is(equalTo(100L)));

    }

    @Test(timeout = 2_000L)
    public void testIsValid() throws Exception {
        System.out.println("testMultiline");
        InputStream is = new ByteArrayInputStream(MULTI_LINE.getBytes());
        LineSource lineSource = new LinesInputStream(is, UTF_8);

        lineSource.stream()
                .map(LogLine::of)
                .filter(LogLine::isValid)
                .forEach(t -> assertTrue("", t.isValid()));
    }

    @Test(timeout = 2_000L)
    public void testHasQuery() throws Exception {
        System.out.println("testHasQuery");
        InputStream is = new ByteArrayInputStream(MULTI_LINE.getBytes());
        LineSource lineSource = new LinesInputStream(is, UTF_8);

        lineSource.stream()
                .map(LogLine::of)
                .filter(LogLine::isValid)
                .forEach(t -> assertNotNull("", t.getQuery() ));
    }
}
