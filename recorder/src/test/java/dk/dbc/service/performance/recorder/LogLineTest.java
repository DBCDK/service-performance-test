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

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class LogLineTest {

    private static final String OK_LINE = "{\"timestamp\":\"2019-03-13T09:33:14.546+00:00\",\"version\":\"1\",\"message\":\"[REDACTED]  webapp=/solr path=/select params={q=REDACTED&defType=edismax&fl=REDACTED&start=0&fq=REDACTED&rows=99999&wt=phps&trackingId=REDACTED} hits=0 status=0 QTime=3\",\"logger\":\"org.apache.solr.core.SolrCore.Request\",\"thread\":\"qtp210506412-1345696\",\"level\":\"INFO\",\"level_value\":20000,\"mdc\":{\"core\":\"x:REDACTED\",\"replica\":\"r:REDACTED\",\"node_name\":\"n:REDACTED\",\"collection\":\"c:REDACTED\",\"shard\":\"s:shard8\"},\"app\":\"solr7\"}";
    private static final String DISTRIB_LINE = "{\"timestamp\":\"2019-03-13T09:33:14.556+00:00\",\"version\":\"1\",\"message\":\"[REDACTED]  webapp=/solr path=/select params={df=term.default&distrib=false&_stateVer_=REDACTED&debug=false&fl=REDACTED&fl=REDACTED&shards.purpose=68&start=0&fsv=true&q.op=AND&shard.url=http://REDACTED&rows=10000&version=2&q=REDACTED&NOW=1552469594555&isShard=true&wt=javabin} hits=0 status=0 QTime=0\",\"logger\":\"org.apache.solr.core.SolrCore.Request\",\"thread\":\"qtp210506412-1411563\",\"level\":\"INFO\",\"level_value\":20000,\"mdc\":{\"node_name\":\"n:REDACTED\",\"core\":\"x:REDACTED\",\"collection\":\"c:REDACTED\",\"shard\":\"s:shard10\",\"replica\":\"r:REDACTED\"},\"app\":\"solr7\"}";
    private static final String JVM_LINE = "{\"timestamp\":\"2019-03-13T09:33:14.522+00:00\",\"version\":\"1\",\"message\":\"[MP][qtp210506412-1172747]:   seg=_j6r(7.6.0):C12777/6360 size=2.047 MB\",\"logger\":\"org.apache.solr.update.LoggingInfoStream\",\"thread\":\"qtp210506412-1172747\",\"level\":\"INFO\",\"level_value\":20000,\"mdc\":{\"node_name\":\"n:REDACTED\",\"core\":\"x:REDACTED\",\"collection\":\"c:REDACTED\",\"shard\":\"s:shard5\",\"replica\":\"r:REDACTED\"},\"app\":\"solr7\"}";
    private static final String UPDATE_LINE = "{\"timestamp\":\"2019-03-13T09:33:14.535+00:00\",\"version\":\"1\",\"message\":\"[REDACTED]  webapp=/solr path=/update params={update.distrib=FROMLEADER&update.chain=timestamp&distrib.from=http://REDACTED&wt=javabin&version=2}{add=[51086821/32!870970-basis-51086821 (1627882357458468864)]} 0 60\",\"logger\":\"org.apache.solr.update.processor.LogUpdateProcessorFactory\",\"thread\":\"qtp210506412-1100619\",\"level\":\"INFO\",\"level_value\":20000,\"mdc\":{\"node_name\":\"n:REDACTED\",\"core\":\"x:REDACTED\",\"collection\":\"c:REDACTED\",\"shard\":\"s:shard4\",\"replica\":\"r:REDACTED\"},\"app\":\"solr7\"}";

    @Test(timeout = 2_000L)
    public void testDistrib() throws Exception {
        System.out.println("testDistrib");
        LogLine logLine = LogLine.of(DISTRIB_LINE);
        assertThat(logLine.isValid(), is(false));
    }

    @Test(timeout = 2_000L)
    public void testStripTrackingId() throws Exception {
        System.out.println("testStripTrackingId");

        assertThat(OK_LINE, containsString("trackingId=REDACTED"));
        LogLine logLine = LogLine.of(OK_LINE);
        assertThat(logLine.isValid(), is(true));
        assertThat(logLine.getQuery(), not(containsString("trackingId=REDACTED")));
    }

    @Test(timeout = 2_000L)
    public void testAddMarker() throws Exception {
        System.out.println("testAddMarker");

        assertThat(OK_LINE, not(containsString("dbcPerfTest")));
        LogLine logLine = LogLine.of(OK_LINE);
        assertThat(logLine.isValid(), is(true));
        assertThat(logLine.getQuery(), containsString("dbcPerfTest=true"));
    }

    @Test(timeout = 2_000L)
    public void testNotRequest() throws Exception {
        System.out.println("testNotRequest");
        LogLine logLine = LogLine.of(JVM_LINE);
        assertThat(logLine.isValid(), is(false));
    }

    @Test(timeout = 2_000L)
    public void testNotSelect() throws Exception {
        System.out.println("testNotSelect");
        LogLine logLine = LogLine.of(UPDATE_LINE);
        assertThat(logLine.isValid(), is(false));
    }

    @Test(timeout = 2_000L)
    public void testAppName() throws Exception {
        System.out.println("testAppName");
        LogLine logLine = LogLine.of(OK_LINE);
        assertThat(logLine.getApp(), is("solr7"));
    }
}
