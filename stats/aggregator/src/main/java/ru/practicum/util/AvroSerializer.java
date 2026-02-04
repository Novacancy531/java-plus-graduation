package ru.practicum.util;

import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;

public class AvroSerializer {

    public static <T extends SpecificRecord> byte[] serialize(T record) {
        try (var out = new ByteArrayOutputStream()) {
            var writer = new SpecificDatumWriter<>(record.getSchema());
            var encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(record, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
