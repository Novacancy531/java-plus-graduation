package ru.practicum.collector;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.stats.proto.ActionTypeProto;
import ru.practicum.stats.proto.UserActionControllerGrpc;
import ru.practicum.stats.proto.UserActionProto;

import java.time.Instant;

@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    private void collect(long userId, long eventId, ActionTypeProto type) {
        var now = Instant.now();
        client.collectUserAction(
                UserActionProto.newBuilder()
                        .setUserId(userId)
                        .setEventId(eventId)
                        .setActionType(type)
                        .setTimestamp(Timestamp.newBuilder()
                                .setSeconds(now.getEpochSecond())
                                .setNanos(now.getNano())
                                .build())
                        .build()
        );
    }

    public void register(long userId, long eventId) {
        collect(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

    public void view(long userId, long eventId) {
        collect(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    public void like(long userId, long eventId) {
        collect(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }
}
