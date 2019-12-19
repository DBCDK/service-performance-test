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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Replay the recorded queries against a service instance
 * and record execution time and result.
 * <p>
 * Various means of aborting the test exist through the config object (Command
 * line)
 * These are
 * - Limit the number of queries sendt
 * - Limit the total time during test
 * - Abort if a query takes longer than x milliseconds
 *
 * @author Mike Andersen (mran@dbc.dk)
 */
public class Replayer implements JobListener {

    private static final Logger log = LoggerFactory.getLogger(Replayer.class);

    private final Config config;
    private boolean callTimeExceeded = false;

    public Replayer(Config config) {
        this.config = config;
    }

    /** Run the test, and record the result
     *
     * @return 0 for complete run, greater than 0 if the test was stopped
     *         prematurely
     */
    public int run() {
        LogCollector logCollector = new LogCollector();
        CallTimeWathcer wathcer = new CallTimeWathcer(config.getCallBufferSize(), config.getMaxDelayedCalls(), config.getCallTimeConstraint());
        String input = config.getInput();
        ExecutorService executorService = Executors.newCachedThreadPool();

        logCollector.addConfig(config.asMap());
        Status runStatus = new Status();

        if (!fileExistsAndNotDir(input)) {
            runStatus.setStatus(Status.Code.IOERROR, "File " + input + " does not exist or is not a file");
        } else {
            Instant timeStarted = Instant.now();

            try (BufferedReader br = getBufferedReader(input)) {
                long numLines = 0;
                while (br.ready()) {
                    LogCollector.LogEntry logEntry = LogCollector.newEntry();

                    if (callTimeExceeded) {
                        runStatus.setStatus(Status.Code.CALLTIME_EXCEEDED, "CallTime exceeded (" + config.getCallTimeConstraint() + "ms)");
                        break;
                    }

                    numLines++;
                    if (numLines > config.getLimit()) {
                        runStatus.setStatus(Status.Code.MAXLINES_EXCEEDED, "Max number of line constraint exceeded (" + config.getLimit() + " lines)");
                        break;
                    }

                    LogLine logLine = LogLine.of(br.readLine());
                    if (!logLine.isValid()) {
                        log.error("Logline not valid logline=" + logLine);
                        continue;
                    }

                    logCollector.addEntry(logEntry);
                    ReplayerTask task = new ReplayerTask(config, logCollector, wathcer, logLine, this, logEntry);
                    executorService.execute(task);

                    long runtime = Duration.between(timeStarted, Instant.now()).toMillis();
                    long originalTimeDelta = logLine.getTimeDelta();
                    long callDelay = calculateDelay(runtime, originalTimeDelta);
                    logEntry.setTimes(originalTimeDelta, callDelay);

                    log.info("Sleeping for {}ms", callDelay);
                    try {
                        Thread.sleep(callDelay);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Interrupted!!!");
                    }

                    if (hasExceededDuration(timeStarted)) {
                        log.info("Runtime exceeded - Aborting!");
                        runStatus.setStatus(Status.Code.RUNTIME_EXCEEDED, "Runtime exceeded (" + config.getDurationConstraint() + "ms)");
                        break;
                    }
                }
            } catch (IOException ex) {
                runStatus.setStatus(Status.Code.IOERROR, "Error processing input: " + ex.getMessage());
            }
        }

        if (!runStatus.statusOK()) {
            logCollector.addStatusEntry(runStatus.getMessage());
            log.error(runStatus.getMessage());
        }

        try {
            executorService.shutdown();
            executorService.awaitTermination(5L, TimeUnit.SECONDS);
            logCollector.addRunStatus(runStatus.getCode(), runStatus.getMessage());
            logCollector.dump(getDestination(config.getOutput()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return runStatus.getCode();
    }

    /**
     * Calculate the actual delay between calls
     *
     * @param runtime           The current accumulated runtime
     * @param originalTimeDelta The original time delta from the start
     * @return The calculated delay to wait, adjusted for the configured
     *         replay speed
     */
    private long calculateDelay(long runtime, long originalTimeDelta) {

        long nextRequestRealTime = (long) ( (double) originalTimeDelta  * config.getReplay());
        log.debug("replayTime: {}, realTime: {}", originalTimeDelta, nextRequestRealTime);
        return Long.max(0, nextRequestRealTime - runtime);
    }

    private BufferedReader getBufferedReader(String input) throws FileNotFoundException {
        InputStream is = (InputStream) ( ( input != null ) ? new FileInputStream(input) : System.in );
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    private boolean fileExistsAndNotDir(String input) {
        File f = new File(input);
        if (f.exists() && !f.isDirectory())
            return true;
        else
            return false;
    }

    private OutputStream getDestination(String fileName) throws FileNotFoundException {
        if (fileName == null)
            return null;
        else
            return new FileOutputStream(fileName);
    }

    /**
     * Check if the program has exceeded the configured max duration
     *
     * @param timeStarted Time since program start
     * @return true/false has the program exceeded the runtime constraint
     */
    private boolean hasExceededDuration(Instant timeStarted) {
        return timeOffsetMS(timeStarted, Instant.now()) > config.getDurationConstraint();
    }

    /**
     * Calculate difference (age) between two timestamps in ms
     *
     * @param begin Start TS as an Instant
     * @param end   End TS as an Instant
     * @return milliseconds
     */
    public long timeOffsetMS(Instant begin, Instant end) {
        return Duration.between(begin, end).toMillis();
    }

    /**
     * Callback from ReplayerTask
     */
    @Override
    public void callTimeExceeded() {
        callTimeExceeded = true;
    }

    private static class Status {

        public enum Code {
            OK(0),
            RUNTIME_EXCEEDED(50),
            CALLTIME_EXCEEDED(51),
            MAXLINES_EXCEEDED(52),
            IOERROR(1);
            private final int value;

            private Code(int value) {
                this.value = value;
            }

            public int getValue() {
                return value;
            }
        }

        public Status() {
            this.code = Code.OK;
            this.message = "";
        }

        public void setStatus(Code code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public boolean statusOK() {
            return code == Code.OK;
        }

        public int getCode() {
            return code.getValue();
        }

        private String message;
        private Code code;
    }
}
