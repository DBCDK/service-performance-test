
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
 * File created: 14/03/2019
 */
package dk.dbc.service.performance.replayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Accept log-lines and send the contained query to a service instance.
 * Record the execution time, and record the result
 */
public class ServiceSender {
    private static final Logger log = LoggerFactory.getLogger(ServiceSender.class);

    private String baseUrl;
    private LogCollector logCollector;


    /**
     * @param baseUrl Base service url
     * @param collector A Log-collector
     */
    public ServiceSender(String baseUrl, LogCollector collector) {
        this.baseUrl = baseUrl;
        this.logCollector = collector;
    }

    /**
     * Send a query to the service and capture information about the request
     * @param logLine a Line from the recorded log
     * @return Duration of the service call in ms
     */
    public long send(LogLine logLine, LogCollector.LogEntry logEntry ) {
        log.trace( "LogLine = " + logLine);

        long callDuration = 0;
        final String q = logLine.getQuery();
        logEntry.setQuery(q);

        try {
            URL url = new URL(baseUrl + q);
            HttpURLConnection client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("GET");

            Timer.start();
            client.connect();
            int responseCode = client.getResponseCode();

            logCollector.incrementFor(Integer.toString(responseCode));

            if (responseCode != 200) {
                log.error( "Got non-200 status({}) from service on query: {}", responseCode, q);
            }

            logEntry.setStatus(Integer.toString(responseCode));
        } catch (Exception e) {
            log.error("Exception from client caught ({}) on query: {}", e.getMessage() , q);
            logEntry.setStatus("Exception from http-client (" + e.getMessage() + ")");
        }

        finally {
            callDuration = Timer.end();
            logEntry.setCallDuration(callDuration);

            log.info( "Call duration = {}ms", callDuration);
        }

        return callDuration;
    }
    private static class Timer {
        private static long startTS;

        public static void start() {
            startTS = System.currentTimeMillis();
        }

        public static long end() {
            return System.currentTimeMillis() - startTS;
        }
    }
}
