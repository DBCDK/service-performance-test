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
 * File created: 26/03/2019
 */

/**
 * Logs the calltime for the latest x calls to the service
 * and throws an exception if more than y calls exceed
 * the given limit
 */
public class CallTimeWathcer {
    private CallStack callTimes;
    private int maxCalls;
    private long cutoffLimit;

    /**
     *
     * @param callBufferSize Number of calls to log/watch
     * @param maxCalls Max number of calls exceeding cutoffLimit accepted
     * @param cutoffLimit Max call time allowed
     */
    public CallTimeWathcer(int callBufferSize, int maxCalls, long cutoffLimit) {
        this.callTimes = new CallStack(callBufferSize);
        this.maxCalls = maxCalls;
        this.cutoffLimit = cutoffLimit;
    }

    public synchronized void addCallTime(long callTime) {
        callTimes.push(callTime);
        if( callTimes.numElementsAbove(cutoffLimit) > this.maxCalls) {
            throw new CallTimeExceededException();
        }
    }

    /**
     * Count the number of elements in the callStack containing elements with
     * a value greater than or equal cutoffLimit
     *
     * @param cutoffLimit The limit to test against
     * @return Number of elements
     */
    public long numElementsAbove(long cutoffLimit) {
        return callTimes.numElementsAbove(cutoffLimit);
    }

    static class CallStack extends FixedSizeStack<Long> {

        public CallStack(int size) {
            super(size);
        }

        public long numElementsAbove(long cutoffLimit) {
            return stack.stream()
                    .filter( e-> {if(e >= cutoffLimit) return true; else return false;})
                    .count();
        }
    }
}
