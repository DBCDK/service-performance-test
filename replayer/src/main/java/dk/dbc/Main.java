/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of performance-test-replayer
 *
 * performance-test-replayer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * performance-test-replayer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc;

import dk.dbc.service.performance.replayer.Config;
import dk.dbc.service.performance.replayer.Replayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master entry point
 *
 * @author Mike Andersen (mran@dbc.dk)
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Config config = Config.of(args);
        log.info("start");
        int exitcode = new Replayer(config).run();
        log.info("end");

        System.exit(exitcode);
    }
}
