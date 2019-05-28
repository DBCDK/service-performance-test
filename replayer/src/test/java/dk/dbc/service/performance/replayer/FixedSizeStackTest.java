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
 * File created: 26/03/2019
 */
package dk.dbc.service.performance.replayer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FixedSizeStackTest {
    FixedSizeStack<Integer> stack;

    @Before
    public void setUp() throws Exception {
        stack = new FixedSizeStack<Integer>(10);

        // Add 10 elements
        for( int x=0; x<10; x++)
            stack.push(x);
        assertEquals(stack.size(), 10);
    }

    @Test
    public void testAddManyAndKeepSize() {
        // add 4 more element
        stack.push(10);
        stack.push(11);
        stack.push(12);
        stack.push(13);
        assertEquals(stack.size(), 10);
    }

    @Test
    public void testStackContentAndSize() {//NOPMD
        // add 4 more element
        stack.push(10);
        stack.push(11);
        stack.push(12);
        stack.push(13);

        assertEquals(stack.pop().intValue(), 13);
        assertEquals(stack.pop().intValue(), 12);
        assertEquals(stack.pop().intValue(), 11);
        assertEquals(stack.peekTop().intValue(), 10);
    }

    @Test
    public void testStackContent() {
        // add 4 more element
        stack.push(10);
        stack.push(11);
        stack.push(12);
        stack.push(13);

        Integer actual;
        for( Integer expected=13; expected > 3; expected--) {
            actual=stack.pop();
            System.out.println( "expected=" + expected + " actual=" + actual);
            assertNotNull(actual);
            assertEquals( expected, actual);
        }

        assertEquals(stack.size(), 0);
    }
}
