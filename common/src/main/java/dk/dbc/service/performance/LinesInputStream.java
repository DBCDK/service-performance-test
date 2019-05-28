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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link LineSource} from an {@link InputStream}
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class LinesInputStream extends LineSource {

    private static final Logger log = LoggerFactory.getLogger(LinesInputStream.class);

    private final BufferedReader reader;

    public LinesInputStream(InputStream is) {
        this(is, StandardCharsets.UTF_8);
    }

    public LinesInputStream(InputStream is, Charset charset) {
        this.reader = new BufferedReader(new InputStreamReader(is, charset));
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException ex) {
            log.error("Error closing source: {}", ex.getMessage());
            log.debug("Error closing source: ", ex);
        }
    }

    @Override
    protected String nextLine() throws IOException {
        for (;;) {
            String line = reader.readLine();
            if (line == null || !line.isEmpty())
                return line;
        }
    }
}
