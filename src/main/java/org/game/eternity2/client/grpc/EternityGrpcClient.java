package org.game.eternity2.client.grpc;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.game.eternity2.grpc.EternityServiceGrpc;
import org.game.eternity2.grpc.FlatBufferRequest;
import org.game.eternity2.grpc.FlatBufferResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * A wrapper around the gRPC client stub to handle connection and FlatBuffers
 * wrapping.
 */
public class EternityGrpcClient {
    private static final Logger logger = LogManager.getLogger(EternityGrpcClient.class);

    private final ManagedChannel channel;
    private final EternityServiceGrpc.EternityServiceBlockingStub blockingStub;
    private final EternityServiceGrpc.EternityServiceStub asyncStub;

    public EternityGrpcClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS
                // to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    EternityGrpcClient(ManagedChannel channel) {
        this.channel = channel;
        this.blockingStub = EternityServiceGrpc.newBlockingStub(channel);
        this.asyncStub = EternityServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Sends a raw byte array payload (FlatBuffers) to the Login endpoint.
     */
    public byte[] login(byte[] flatBuffersPayload) {
        logger.info("Sending Login request via gRPC...");
        FlatBufferRequest request = FlatBufferRequest.newBuilder()
                .setPayload(ByteString.copyFrom(flatBuffersPayload))
                .build();

        FlatBufferResponse response;
        try {
            response = blockingStub.login(request);
            return response.getPayload().toByteArray();
        } catch (Exception e) {
            logger.error("RPC failed: " + e.getMessage());
            throw e;
        }
    }

    // TODO: Add other methods (getJob, submitSolution) similarly
}
