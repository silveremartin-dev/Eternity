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
package org.game.eternity2.server.grpc;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.game.eternity2.grpc.EternityServiceGrpc;
import org.game.eternity2.grpc.FlatBufferRequest;
import org.game.eternity2.grpc.FlatBufferResponse;
import org.game.eternity2.server.EternityServer;

import java.util.logging.Logger;

/**
 * Implementation of the gRPC EternityService.
 * Handles incoming gRPC requests, deserializes FlatBuffers payloads,
 * and delegates to the core server logic.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class EternityServiceImpl extends EternityServiceGrpc.EternityServiceImplBase {

    private static final Logger LOGGER = Logger.getLogger(EternityServiceImpl.class.getName());

    @SuppressWarnings("unused") // Reserved for future gRPC-to-server delegation
    private final EternityServer server;

    public EternityServiceImpl(EternityServer server) {
        this.server = server;
    }

    @Override
    public void login(FlatBufferRequest request, StreamObserver<FlatBufferResponse> responseObserver) {
        try {
            // 1. Extract FlatBuffers payload
            // byte[] payloadBytes = request.getPayload().toByteArray(); // Unused currently
            // Payload ready for FlatBuffers deserialization when implemented

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
        try {
            LOGGER.info("Received getJob request via gRPC");

            byte[] responseBytes = new byte[0];
            if (server != null && server.getJobManager() != null) {
                org.game.eternity2.server.Job job = server.getJobManager().getNextJob("grpc-client");
                if (job != null && job.getInitialBoard() != null) {
                    responseBytes = org.game.eternity2.io.FlatBuffersSerializer.serialize(job.getInitialBoard());
                }
            }

            FlatBufferResponse response = FlatBufferResponse.newBuilder()
                    .setPayload(ByteString.copyFrom(responseBytes))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            LOGGER.severe("Error in getJob: " + e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void submitSolution(FlatBufferRequest request, StreamObserver<FlatBufferResponse> responseObserver) {
        try {
            LOGGER.info("Received submitSolution request via gRPC");

            if (request.getPayload() != null && !request.getPayload().isEmpty()) {
                try {
                    org.game.eternity2.model.BoardPrimitive board = org.game.eternity2.io.FlatBuffersSerializer.deserialize(request.getPayload().toByteArray());
                    if (server != null && board != null) {
                        server.updateMasterBoard(board);
                    }
                } catch (Exception parseEx) {
                    LOGGER.warning("Could not deserialize submitted solution board: " + parseEx.getMessage());
                }
            }

            byte[] responseBytes = new byte[0];

            FlatBufferResponse response = FlatBufferResponse.newBuilder()
                    .setPayload(ByteString.copyFrom(responseBytes))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            LOGGER.severe("Error in submitSolution: " + e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void keepAlive(FlatBufferRequest request, StreamObserver<FlatBufferResponse> responseObserver) {
        try {
            LOGGER.fine("Received keepAlive request via gRPC");

            // Return heartbeat acknowledgement
            byte[] responseBytes = new byte[0];

            FlatBufferResponse response = FlatBufferResponse.newBuilder()
                    .setPayload(ByteString.copyFrom(responseBytes))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            LOGGER.severe("Error in keepAlive: " + e.getMessage());
            responseObserver.onError(e);
        }
    }
}
