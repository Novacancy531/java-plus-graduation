package ru.practicum.service;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.proto.ActionTypeProto;
import ru.practicum.stats.proto.UserActionControllerGrpc;
import ru.practicum.stats.proto.UserActionProto;
import ru.practicum.util.AvroSerializer;

import java.time.Instant;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserActionControllerImpl
        extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final KafkaProducer<Long, byte[]> producer;
    private final CollectorKafkaProperties props;


    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {

        try {
            log.info("Collector: received user-action userId={} eventId={} type={} ts={}",
                    request.getUserId(),
                    request.getEventId(),
                    request.getActionType(),
                    request.getTimestamp());
            UserActionAvro avro = map(request);
            byte[] bytes = AvroSerializer.serialize(avro);

            log.info("Send user action: key={}, topic={}", key(avro), topic());

            producer.send(new ProducerRecord<>(topic(), key(avro), bytes));
            log.info("Collector: sent user-action key={} topic={}", key(avro), topic());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("Ошибка обработки запроса: {}", request, e);
            responseObserver.onError(e);
        }
    }

    private static long key(UserActionAvro a) {
        return a.getUserId();
    }

    private static ActionTypeAvro mapType(ActionTypeProto t) {
        return switch (t) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> ActionTypeAvro.VIEW;
        };
    }

    private String topic() {
        return props.getTopics().getUserActions();
    }

    private UserActionAvro map(UserActionProto p) {
        return UserActionAvro.newBuilder()
                .setUserId(p.getUserId())
                .setEventId(p.getEventId())
                .setActionType(mapType(p.getActionType()))
                .setTimestamp(
                        Instant.ofEpochSecond(
                                p.getTimestamp().getSeconds(),
                                p.getTimestamp().getNanos()
                        )
                )
                .build();
    }
}
