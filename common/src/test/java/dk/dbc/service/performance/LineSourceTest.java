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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class LineSourceTest {

    private static final Logger log = LoggerFactory.getLogger(LineSourceTest.class);

    @Test(timeout = 2_000L)
    public void testInput() throws Exception {
        System.out.println("testInput");

        try (InputStream is = new ByteArrayInputStream(
                ( "123\n" +
                  "234\n" +
                  "345\n" +
                  "\n" +
                  "456" ).getBytes(StandardCharsets.UTF_8)) ;
             LinesInputStream lineSource = new LinesInputStream(is)) {
            long count = lineSource.stream()
                    .count();
            assertThat(count, is(4L));
        }
    }

}
