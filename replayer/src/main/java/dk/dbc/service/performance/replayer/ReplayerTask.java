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
 * File created: 25/03/2019
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An asynchronous task designed to call a service instance,
 * log and monitor the duration of the calls
 */
public class ReplayerTask implements Runnable{
    private static final Logger log = LoggerFactory.getLogger(ReplayerTask.class);

    private ServiceSender sender;
    private LogLine logLine;
    private CallTimeWathcer watcher;
    private JobListener jobListener;

    public ReplayerTask(Config config, LogCollector logCollector, CallTimeWathcer watcher, LogLine logLine, JobListener jobListener) {
        this.watcher = watcher;

        this.sender = new ServiceSender(config.getService(), logCollector);
        this.logLine = logLine;
        this.jobListener = jobListener;
    }

    @Override
    public void run() {
        log.debug( "Running: logLine=" + logLine);
        long duration = sender.send(logLine);
        try {
            watcher.addCallTime(duration); // Can throw CallTimeExceededException
        } catch (CallTimeExceededException ex ) {
            notifyListeners();
        }
    }

    private void notifyListeners() {
        jobListener.callTimeExceeded();
    }
}
