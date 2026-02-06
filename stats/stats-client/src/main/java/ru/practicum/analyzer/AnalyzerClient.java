package ru.practicum.analyzer;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.stats.proto.InteractionsCountRequestProto;
import ru.practicum.stats.proto.RecommendedEventProto;
import ru.practicum.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.stats.proto.SimilarEventsRequestProto;
import ru.practicum.stats.proto.UserPredictionsRequestProto;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        var req = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(client.getRecommendationsForUser(req), Spliterator.ORDERED),
                false
        );
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        var req = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(client.getSimilarEvents(req), Spliterator.ORDERED),
                false
        );
    }

    public Stream<RecommendedEventProto> getInteractionsCount(Iterable<Long> eventIds) {
        var req = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(client.getInteractionsCount(req), Spliterator.ORDERED),
                false
        );
    }
}
