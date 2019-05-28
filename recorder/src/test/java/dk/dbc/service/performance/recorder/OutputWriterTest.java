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

import dk.dbc.service.performance.LineSource;
import dk.dbc.service.performance.LinesInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class OutputWriterTest {

    @Test(timeout = 2_000L)
    public void testEof() throws Exception {
        System.out.println("testEof");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputWriter outputWriter = new OutputWriter(bos, 1500, Long.MAX_VALUE, Long.MAX_VALUE, OutputWriterTest::firstLine);
             InputStream is = getClass().getClassLoader().getResourceAsStream("log.data");
             LineSource lineSource = new LinesInputStream(is, UTF_8)) {

            lineSource.stream()
                    .map(LogLine::of)
                    .filter(LogLine::isValid)
                    .forEach(outputWriter);
        }
        String content = new String(bos.toByteArray(), UTF_8);
        String[] array = content.split("\n");
        System.out.println("array.length = " + array.length);
        assertThat(array.length, greaterThan(50));
        assertThat(content, startsWith("0 ")); // Ensure timing is right
    }

    @Test(timeout = 2_000L)
    public void testDuration() throws Exception {
        System.out.println("testDuration");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputWriter outputWriter = new OutputWriter(bos, 1500, 1_500L, Long.MAX_VALUE, OutputWriterTest::firstLine) ;
             InputStream is = getClass().getClassLoader().getResourceAsStream("log.data") ;
             LineSource lineSource = new LinesInputStream(is, UTF_8)) {

            lineSource.stream()
                    .map(LogLine::of)
                    .filter(LogLine::isValid)
                    .forEach(outputWriter);
        } catch (CompletedException ex) {
            System.out.println("completed");
        }
        String content = new String(bos.toByteArray(), UTF_8);
        System.out.println("content = " + content);
        String[] array = content.split("\n");
        System.out.println("array.length = " + array.length);
        assertThat(array.length, lessThan(50));
        assertThat(content, startsWith("0 ")); // Ensure timing is right
    }

    @Test(timeout = 2_000L)
    public void testLines() throws Exception {
        System.out.println("testLines");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputWriter outputWriter = new OutputWriter(bos, 10, Long.MAX_VALUE, 25, OutputWriterTest::firstLine) ;
             InputStream is = getClass().getClassLoader().getResourceAsStream("log.data") ;
             LineSource lineSource = new LinesInputStream(is, UTF_8)) {

            lineSource.stream()
                    .map(LogLine::of)
                    .filter(LogLine::isValid)
                    .forEach(outputWriter);
        } catch (CompletedException ex) {
            // completed duration
        }
        String content = new String(bos.toByteArray(), UTF_8);
        System.out.println("content = " + content);
        String[] array = content.split("\n");
        System.out.println("array.length = " + array.length);
        assertThat(array.length, is(25));
        assertThat(content, startsWith("0 ")); // Ensure timing is right
    }

    private static void firstLine(OutputStream os, LogLine logLine) {
        System.out.println("logLine = " + logLine);
    }

}
