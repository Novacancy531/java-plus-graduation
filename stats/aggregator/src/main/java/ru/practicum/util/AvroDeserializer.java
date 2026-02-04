package ru.practicum.util;

import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;

public class AvroDeserializer {

    public static <T extends SpecificRecord> T deserialize(byte[] data, T empty) {
        try {
            var reader = new SpecificDatumReader<T>(empty.getSchema());
            var decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(empty, decoder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
