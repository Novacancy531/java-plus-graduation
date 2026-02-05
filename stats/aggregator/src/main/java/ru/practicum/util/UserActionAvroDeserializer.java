package ru.practicum.util;

import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserActionAvroDeserializer implements Deserializer<UserActionAvro> {
    @Override
    public UserActionAvro deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        return AvroDeserializer.deserialize(data, new UserActionAvro());
    }
}
