package ru.practicum.kafka.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.model.UserEventInteraction;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.OffsetDateTime;

@Component
public class UserActionAvroMapper {

    public UserEventInteraction map(UserActionAvro avro) {
        UserEventInteraction e = new UserEventInteraction();
        e.setUserId(avro.getUserId());
        e.setEventId(avro.getEventId());
        e.setWeight(weight(avro));
        e.setLastTs(
                OffsetDateTime.ofInstant(
                        avro.getTimestamp(),
                        OffsetDateTime.now().getOffset()
                )
        );
        return e;
    }

    private double weight(UserActionAvro avro) {
        return switch (avro.getActionType().toString()) {
            case "VIEW" -> 0.4;
            case "REGISTER" -> 0.8;
            case "LIKE" -> 1.0;
            default -> 0.0;
        };
    }
}
