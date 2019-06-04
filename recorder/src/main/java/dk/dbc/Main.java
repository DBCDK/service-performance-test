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
package dk.dbc;

import dk.dbc.service.performance.recorder.Config;
import dk.dbc.service.performance.recorder.Recorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master entry point
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        try {
            Config config = Config.of(args);
            log.debug("config = {}", config);
            log.info("start");
            new Recorder(config).run();
            log.info("end");
        } catch (ExitException e) {
            System.exit(e.getCode());
        }
    }

}
