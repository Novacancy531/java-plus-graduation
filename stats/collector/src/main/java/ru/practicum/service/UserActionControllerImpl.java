package ru.practicum.service;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.proto.ActionTypeProto;
import ru.practicum.stats.proto.UserActionControllerGrpc;
import ru.practicum.stats.proto.UserActionProto;
import ru.practicum.util.AvroSerializer;

import java.time.Instant;

@GrpcService
public class UserActionControllerImpl
        extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final KafkaProducer<Long, byte[]> producer;
    private final String topic;

    private static final Logger log =
            LoggerFactory.getLogger(UserActionControllerImpl.class);

    public UserActionControllerImpl(
            KafkaProducer<Long, byte[]> producer,
            CollectorKafkaProperties props
    ) {
        this.producer = producer;
        this.topic = props.getTopics().getUserActions();
    }

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {

        log.info("Collector: received user-action userId={} eventId={} type={} ts={}",
                request.getUserId(),
                request.getEventId(),
                request.getActionType(),
                request.getTimestamp());
        UserActionAvro avro = map(request);
        byte[] bytes = AvroSerializer.serialize(avro);

        log.info("Send user action: key={}, topic={}", key(avro), topic);

        try {
            producer.send(new ProducerRecord<>(topic, key(avro), bytes));
            log.info("Collector: sent user-action key={} topic={}", key(avro), topic);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to send user action to Kafka", e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Failed to send user action")
                            .withCause(e)
                            .asRuntimeException()
            );
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
