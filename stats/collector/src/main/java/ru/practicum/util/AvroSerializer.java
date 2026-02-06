package ru.practicum.util;

import org.apache.avro.data.TimeConversions;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;

public class AvroSerializer {

    public static <T extends SpecificRecord> byte[] serialize(T record) {
        try (var out = new ByteArrayOutputStream()) {
            var data = SpecificData.getForClass(record.getClass());
            data.addLogicalTypeConversion(new TimeConversions.TimestampMillisConversion());
            var writer = new SpecificDatumWriter<>(record.getSchema(), data);
            var encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(record, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
