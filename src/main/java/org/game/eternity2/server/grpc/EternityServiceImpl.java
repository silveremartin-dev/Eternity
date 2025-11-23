package org.game.eternity2.server.grpc;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.game.eternity2.grpc.EternityServiceGrpc;
import org.game.eternity2.grpc.FlatBufferRequest;
import org.game.eternity2.grpc.FlatBufferResponse;
import org.game.eternity2.proto.Message; // FlatBuffers generated class
import org.game.eternity2.proto.LoginRequest;
import org.game.eternity2.proto.LoginResponse;
import org.game.eternity2.server.EternityServer;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Implementation of the gRPC EternityService.
 * Handles incoming gRPC requests, deserializes FlatBuffers payloads,
 * and delegates to the core server logic.
 */
public class EternityServiceImpl extends EternityServiceGrpc.EternityServiceImplBase {

    private static final Logger LOGGER = Logger.getLogger(EternityServiceImpl.class.getName());
    private final EternityServer server;

    public EternityServiceImpl(EternityServer server) {
        this.server = server;
    }

    @Override
    public void login(FlatBufferRequest request, StreamObserver<FlatBufferResponse> responseObserver) {
        try {
            // 1. Extract FlatBuffers payload
            byte[] payloadBytes = request.getPayload().toByteArray();
            ByteBuffer bb = ByteBuffer.wrap(payloadBytes);

            // 2. Deserialize (Zero-Copy read)
            // Note: We assume the payload is a 'Message' table as root, or specific request
            // type.
            // For simplicity in this V1, let's assume we parse specific types based on
            // context or a wrapper.
            // Ideally, we should use a Union in FlatBuffers (Message union).

            // For now, let's just acknowledge the login to prove connectivity.
            LOGGER.info("Received Login Request via gRPC");

            // 3. Logic (Mock for now)
            // In a real implementation, we would parse LoginRequest from bb
            // LoginRequest loginReq = LoginRequest.getRootAsLoginRequest(bb);

            // 4. Serialize Response (FlatBuffers)
            // FlatBufferBuilder builder = new FlatBufferBuilder(1024);
            // ... build LoginResponse ...
            // byte[] responseBytes = builder.sizedByteArray();

            // Mock response payload
            byte[] responseBytes = new byte[0];

            // 5. Wrap in gRPC response
            FlatBufferResponse response = FlatBufferResponse.newBuilder()
                    .setPayload(ByteString.copyFrom(responseBytes))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            LOGGER.severe("Error in login: " + e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void getJob(FlatBufferRequest request, StreamObserver<FlatBufferResponse> responseObserver) {
        // TODO: Implement getJob logic
        super.getJob(request, responseObserver);
    }

    @Override
    public void submitSolution(FlatBufferRequest request, StreamObserver<FlatBufferResponse> responseObserver) {
        // TODO: Implement submitSolution logic
        super.submitSolution(request, responseObserver);
    }

    @Override
    public void keepAlive(FlatBufferRequest request, StreamObserver<FlatBufferResponse> responseObserver) {
        // TODO: Implement keepAlive logic
        super.keepAlive(request, responseObserver);
    }
}
