package dk.dbc.service.performance.replayer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.io.IOException;

public class PercentileSerializer extends StdSerializer<Percentile> {

    public PercentileSerializer(Class<Percentile> t) {
        super(t);
    }

    @Override
    public void serialize(Percentile p, JsonGenerator jsonGen, SerializerProvider provider) throws IOException {
        jsonGen.writeStartObject();
        int[] percentiles = {50, 60, 70, 75, 80, 90, 95, 99};
        for (int pc : percentiles) {
            jsonGen.writeNumberField("percentile " + pc, p.evaluate(pc));
        }
        jsonGen.writeEndObject();
    }


}
