package dk.dbc.service.performance.replayer;
/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of service-performance-test
 *
 * service-performance-test is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * service-performance-test is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * File created: 07/06/2019
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.LongSummaryStatistics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LogCollectorTest {

    private static final List<Long> callDurations = asList(10L, 20L, 30L);
    private LogCollector collector;

    @Before
    public void setUp() throws Exception {
        collector = new LogCollector();
        for( long duration : callDurations) {
            LogCollector.LogEntry logEntry = LogCollector.newEntry();
            logEntry.setCallDuration(duration);
            logEntry.setQuery( "a-query: duration=" + duration);
            collector.addEntry(logEntry);
        }
    }

    @Test(timeout = 2_000L)
    public void testCalculateStats() throws Exception {
        System.out.println( "testCalculateStats" );
        LongSummaryStatistics stat = collector.calculateStats();

        assertThat(stat.getCount(), is(equalTo(3L)));
        assertThat(stat.getAverage(), is(equalTo(20.0)));
        assertThat(stat.getMin(), is(equalTo(10L)));
        assertThat(stat.getMax(), is(equalTo(30L)));
    }

    @Test(timeout = 2_000L)
    public void testDump() throws Exception {
        System.out.println( "testDump" );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        collector.dump(os);

        System.out.println( "Dump: " + os.toString());

        ObjectMapper O = new ObjectMapper();
        JsonNode obj = O.readTree(os.toString());
        JsonNode stats = obj.get("callStat");

        Long count = stats.get("count").asLong();
        assertThat(count, is(equalTo(3L)));

        Long min = stats.get("min").asLong();
        assertThat(min, is(equalTo(10L)));

        Long max = stats.get("max").asLong();
        assertThat(count, is(equalTo(3L)));

        Double avg = stats.get("average").asDouble();
        assertThat(avg, is(equalTo(20.0)));
    }
}
