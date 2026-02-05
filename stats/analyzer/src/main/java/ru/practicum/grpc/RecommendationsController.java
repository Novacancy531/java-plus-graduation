package ru.practicum.grpc;

import io.grpc.stub.StreamObserver;
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
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final SimilarEventsService similarEventsService;
    private final UserRecommendationsService recommendationsService;
    private final InteractionsCountService interactionsCountService;

    public RecommendationsController(SimilarEventsService similarEventsService,
                                     UserRecommendationsService recommendationsService,
                                     InteractionsCountService interactionsCountService) {
        this.similarEventsService = similarEventsService;
        this.recommendationsService = recommendationsService;
        this.interactionsCountService = interactionsCountService;
    }

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        similarEventsService.getSimilar(request.getEventId(), request.getUserId(), request.getMaxResults())
                .forEach(rec -> responseObserver.onNext(
                        RecommendedEventProto.newBuilder()
                                .setEventId(rec.eventId())
                                .setScore(rec.score())
                                .build()
                ));
        responseObserver.onCompleted();
    }

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        recommendationsService.recommend(request.getUserId(), request.getMaxResults())
                .forEach(rec -> responseObserver.onNext(
                        RecommendedEventProto.newBuilder()
                                .setEventId(rec.eventId())
                                .setScore(rec.score())
                                .build()
                ));
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        interactionsCountService.getCounts(request.getEventIdList())
                .forEach((eventId, score) -> responseObserver.onNext(
                        RecommendedEventProto.newBuilder()
                                .setEventId(eventId)
                                .setScore(score)
                                .build()
                ));
        responseObserver.onCompleted();
    }
}
