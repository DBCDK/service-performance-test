package dk.dbc.service.performance.replayer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

public class CallTimeWathcerTest {


    @Before
    public void setUp() throws Exception {

    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void testInsertOk() {
        int numCalls = 10;
        int maxCallsExceeded = 4;
        long cutOffLimit = 100;
        CallTimeWathcer watcher = new CallTimeWathcer(numCalls, maxCallsExceeded, cutOffLimit);

        for( int x=0; x<100; x++) {
            watcher.addCallTime(x);
        }
    }

    @Test
    public void testCallStack() {
        CallTimeWathcer.CallStack c = new CallTimeWathcer.CallStack(10);
        c.push(1L);
        c.push(2L);
        c.push(3L);
        c.push(4L);
        assertEquals( 3, c.numElementsAbove(2));
    }

    @Test(expected = CallTimeExceededException.class)
    public void testThrowsException() {
        int numCalls = 10;
        int maxCallsExceeded = 4;
        long cutOffLimit = 100;
        CallTimeWathcer watcher = new CallTimeWathcer(numCalls, maxCallsExceeded, cutOffLimit);

        for( int x=0; x<100; x++) {
            watcher.addCallTime(5*x);
        }
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void testContainSomeErrors() {
        int numCalls = 10;
        int maxCallsExceeded = 4;
        long cutOffLimit = 100;
        CallTimeWathcer watcher = new CallTimeWathcer(numCalls, maxCallsExceeded, cutOffLimit);

        for( int x=0; x<100; x++) {
            watcher.addCallTime(x);
        }

        watcher.addCallTime(101);
        watcher.addCallTime(102);
        watcher.addCallTime(103);
    }

    @Test
    public void testThrowsWhenExceeding() {
        int numCalls = 10;
        int maxCallsExceeded = 4;
        long cutOffLimit = 100;
        CallTimeWathcer watcher = new CallTimeWathcer(numCalls, maxCallsExceeded, cutOffLimit);

        for( int x=0; x<100; x++) {
            watcher.addCallTime(x);
        }

        watcher.addCallTime(101);
        watcher.addCallTime(102);
        watcher.addCallTime(103);
        watcher.addCallTime(104);
        try {
            watcher.addCallTime(105);
            fail( "CallTimeExceededException not thrown!");
        }
        catch (CallTimeExceededException e) {
            assertEquals(maxCallsExceeded+1, watcher.numElementsAbove(cutOffLimit));
        }
    }
}
