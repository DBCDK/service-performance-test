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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class Recorder {

    private static final Logger log = LoggerFactory.getLogger(Recorder.class);

    private final Config config;

    public Recorder(Config config) {
        this.config = config;
    }

    public void run() {
        try (OutputWriter outputWriter = getOutputWriter()) {
            try (LineSource lineSource = getLineSource()) {
                lineSource.stream()
                        .map(LogLine::of)
                        .filter(LogLine::isValid)
                        .filter(applicationFilter())
                        .forEach(outputWriter);
            } catch (CompletedException ex) {
                log.debug("Completed output");
            } catch (IOException ex) {
                log.error("Error processing input: {}", ex.getMessage());
                log.debug("Error processing input: ", ex);
            }

        } catch (FileNotFoundException ex) {
            log.error("Error opening output: {}", ex.getMessage());
            log.debug("Error opening output: ", ex);
        }
    }

    private OutputWriter getOutputWriter() throws FileNotFoundException {
        OutputStream os;
        String filename = config.getOutput();
        if (filename != null) {
            log.debug("Outputting to {}", filename);
            os = new FileOutputStream(filename, config.isAppend());
        } else {
            log.debug("Outputting to stdout");
            os = System.out;
        }

        return new OutputWriter(os,
                                config.getSortBufferSize(),
                                config.getDuration(),
                                config.getLimit(),
                                new HeaderOutput(config));
    }

    private Predicate<LogLine> applicationFilter() {
        String application = config.getApplication();
        if (application == null)
            return l -> true;
        else
            return l -> application.equals(l.getApp());
    }

    private LineSource getLineSource() throws FileNotFoundException {
        String kafka = config.getKafka();
        String input = config.getInput();
        if (kafka != null) {
            return new LinesKafka(kafka);
        } else if (input != null) {
            return new LinesInputStream(new FileInputStream(input));
        } else {
            return new LinesInputStream(System.in, StandardCharsets.UTF_8);
        }
    }

}
