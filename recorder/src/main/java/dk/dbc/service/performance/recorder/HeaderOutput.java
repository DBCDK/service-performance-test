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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class HeaderOutput implements BiConsumer<OutputStream, LogLine> {

    private static final Logger log = LoggerFactory.getLogger(HeaderOutput.class);

    private static final ObjectMapper O = new ObjectMapper();

    private final Config config;

    public HeaderOutput(Config config) {
        this.config = config;
    }

    @Override
    public void accept(OutputStream os, LogLine logLine) {
        try {
            ObjectNode obj = O.createObjectNode();
            obj.put("started", logLine.getInstant().toString());
            obj.put("from", getSource());
            obj.put("duration", config.getDuration());
            obj.put("lines", config.getLimit());
            String line = new StringBuilder()
                    .append("#")
                    .append(O.writeValueAsString(obj))
                    .append("\n")
                    .toString();
            os.write(line.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            log.error("Not Printing header: {}", ex.getMessage());
            log.debug("Not Printing header: ", ex);
        }
    }

    private String getSource() {
        String kafka = config.getKafka();
        if (kafka != null)
            return "kafka:" + kafka;
        String input = config.getInput();
        if (input != null)
            return "file:" + input;
        return "stdin";
    }

}
