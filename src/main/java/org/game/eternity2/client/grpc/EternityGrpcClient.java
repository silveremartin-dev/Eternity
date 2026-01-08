/*
 * MIT License
 *
 * Copyright (c) 2026 Silvere Martin-Michiellot, Antigravity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class EternityGrpcClient {
    private static final Logger logger = LogManager.getLogger(EternityGrpcClient.class);

    private final ManagedChannel channel;
    private final EternityServiceGrpc.EternityServiceBlockingStub blockingStub;
    @SuppressWarnings("unused")
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

    /**
     * Sends a raw byte array payload (FlatBuffers) to the getJob endpoint.
     */
    public byte[] getJob(byte[] flatBuffersPayload) {
        logger.info("Sending getJob request via gRPC...");
        FlatBufferRequest request = FlatBufferRequest.newBuilder()
                .setPayload(ByteString.copyFrom(flatBuffersPayload))
                .build();

        try {
            FlatBufferResponse response = blockingStub.getJob(request);
            return response.getPayload().toByteArray();
        } catch (Exception e) {
            logger.error("RPC getJob failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Sends a raw byte array payload (FlatBuffers) to the submitSolution endpoint.
     */
    public byte[] submitSolution(byte[] flatBuffersPayload) {
        logger.info("Sending submitSolution request via gRPC...");
        FlatBufferRequest request = FlatBufferRequest.newBuilder()
                .setPayload(ByteString.copyFrom(flatBuffersPayload))
                .build();

        try {
            FlatBufferResponse response = blockingStub.submitSolution(request);
            return response.getPayload().toByteArray();
        } catch (Exception e) {
            logger.error("RPC submitSolution failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Sends a keepAlive heartbeat via gRPC.
     */
    public byte[] keepAlive(byte[] flatBuffersPayload) {
        logger.debug("Sending keepAlive via gRPC...");
        FlatBufferRequest request = FlatBufferRequest.newBuilder()
                .setPayload(ByteString.copyFrom(flatBuffersPayload))
                .build();

        try {
            FlatBufferResponse response = blockingStub.keepAlive(request);
            return response.getPayload().toByteArray();
        } catch (Exception e) {
            logger.error("RPC keepAlive failed: " + e.getMessage());
            throw e;
        }
    }
}
