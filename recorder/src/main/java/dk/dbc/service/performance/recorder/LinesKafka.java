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
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

/**
 * Implementation of {@link LineSource} from a kafka topic
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class LinesKafka extends LineSource {

    private final KafkaConsumer<Long, String> consumer;
    private Iterator<ConsumerRecord<Long, String>> iterator;

    /**
     * Start listening to a kafka topic
     *
     * @param connect connect string of the type
     *                host[:port][,host[:port]]/topic
     */
    public LinesKafka(String connect) {
        String[] parts = connect.split("/", 2);
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                  parts[0]);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                  "SolrPerfTestRecorder");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                  LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                  StringDeserializer.class.getName());
        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singleton(parts[1]));
    }

    @Override
    protected String nextLine() throws IOException {
        while (iterator == null || !iterator.hasNext()) {
            ConsumerRecords<Long, String> records = consumer.poll(60_000L);
            iterator = records.iterator();
        }
        return iterator.next().value();
    }

    @Override
    public void close() {
        this.consumer.unsubscribe();
        this.consumer.close();
    }
}
