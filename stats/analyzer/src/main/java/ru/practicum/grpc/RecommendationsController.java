package ru.practicum.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.stats.proto.InteractionsCountRequestProto;
import ru.practicum.stats.proto.RecommendedEventProto;
import ru.practicum.stats.proto.SimilarEventsRequestProto;
import ru.practicum.stats.proto.UserPredictionsRequestProto;
import ru.practicum.stats.proto.RecommendationsControllerGrpc;

@GrpcService
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        responseObserver.onCompleted();
    }

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {
        responseObserver.onCompleted();
    }
}
