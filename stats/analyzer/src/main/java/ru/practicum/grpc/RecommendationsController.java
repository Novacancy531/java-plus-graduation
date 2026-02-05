package ru.practicum.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.stats.proto.InteractionsCountRequestProto;
import ru.practicum.stats.proto.RecommendedEventProto;
import ru.practicum.stats.proto.SimilarEventsRequestProto;
import ru.practicum.stats.proto.UserPredictionsRequestProto;
import ru.practicum.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.service.InteractionsCountService;
import ru.practicum.service.SimilarEventsService;
import ru.practicum.service.UserRecommendationsService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final SimilarEventsService similarEventsService;
    private final UserRecommendationsService recommendationsService;
    private final InteractionsCountService interactionsCountService;

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            similarEventsService.getSimilar(request.getEventId(), request.getUserId(), request.getMaxResults())
                    .forEach(rec -> responseObserver.onNext(
                            RecommendedEventProto.newBuilder()
                                    .setEventId(rec.eventId())
                                    .setScore(rec.score())
                                    .build()
                    ));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("Ошибка обработки запроса: {}", request, e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            recommendationsService.recommend(request.getUserId(), request.getMaxResults())
                    .forEach(rec -> responseObserver.onNext(
                            RecommendedEventProto.newBuilder()
                                    .setEventId(rec.eventId())
                                    .setScore(rec.score())
                                    .build()
                    ));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("Ошибка обработки запроса: {}", request, e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        try {
            interactionsCountService.getCounts(request.getEventIdList())
                    .forEach((eventId, score) -> responseObserver.onNext(
                            RecommendedEventProto.newBuilder()
                                    .setEventId(eventId)
                                    .setScore(score)
                                    .build()
                    ));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("Ошибка обработки запроса: {}", request, e);
            responseObserver.onError(e);
        }
    }
}
