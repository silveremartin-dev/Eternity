/*
 * Copyright 2022-2024 Silvere Martin-Michiellot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.game.eternity2.server.grpc;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.game.eternity2.grpc.EternityServiceGrpc;
import org.game.eternity2.grpc.FlatBufferRequest;
import org.game.eternity2.grpc.FlatBufferResponse;
import org.game.eternity2.server.EternityServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for EternityService using gRPC in-process server.
 */
class EternityGrpcIntegrationTest {

    private Server server;
    private ManagedChannel channel;
    private EternityServiceGrpc.EternityServiceBlockingStub blockingStub;

    @BeforeEach
    void setUp() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Mocking EternityServer fails on Java 25 (ByteBuddy issue).
        // Since EternityServiceImpl currently doesn't use the server instance (it's
        // unused),
        // we can safely pass null for this integration test.
        EternityServer mockServer = null;

        // Create a server, add service, start, and register for automatic graceful
        // shutdown.
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new EternityServiceImpl(mockServer))
                .build()
                .start();

        // Create a client channel and register for automatic graceful shutdown.
        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        blockingStub = EternityServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown();
            channel.awaitTermination(5, TimeUnit.SECONDS);
        }
        if (server != null) {
            server.shutdown();
            server.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void testLoginFlow() {
        // Prepare request
        byte[] dummyPayload = "login_request".getBytes();
        FlatBufferRequest request = FlatBufferRequest.newBuilder()
                .setPayload(ByteString.copyFrom(dummyPayload))
                .build();

        // Execute gRPC call
        FlatBufferResponse response = blockingStub.login(request);

        // Verify response
        assertNotNull(response);
        assertNotNull(response.getPayload());
        // Currently the implementation mock responds with empty bytes, but ensuring
        // connectivity
        assertEquals(0, response.getPayload().size(), "Expected empty acknowledgment payload for now");
    }

    @Test
    void testGetJobFlow() {
        // Prepare request
        byte[] dummyPayload = "get_job".getBytes();
        FlatBufferRequest request = FlatBufferRequest.newBuilder()
                .setPayload(ByteString.copyFrom(dummyPayload))
                .build();

        // Execute gRPC call
        FlatBufferResponse response = blockingStub.getJob(request);

        // Verify response
        assertNotNull(response);
        assertNotNull(response.getPayload());
        assertEquals(0, response.getPayload().size());
    }

    @Test
    void testSubmitSolutionFlow() {
        // Prepare request
        byte[] dummyPayload = "solution_data".getBytes();
        FlatBufferRequest request = FlatBufferRequest.newBuilder()
                .setPayload(ByteString.copyFrom(dummyPayload))
                .build();

        // Execute gRPC call
        FlatBufferResponse response = blockingStub.submitSolution(request);

        // Verify response
        assertNotNull(response);
        assertNotNull(response.getPayload());
        assertEquals(0, response.getPayload().size());
    }

    @Test
    void testKeepAliveFlow() {
        // Prepare request
        byte[] dummyPayload = "ping".getBytes();
        FlatBufferRequest request = FlatBufferRequest.newBuilder()
                .setPayload(ByteString.copyFrom(dummyPayload))
                .build();

        // Execute gRPC call
        FlatBufferResponse response = blockingStub.keepAlive(request);

        // Verify response
        assertNotNull(response);
        assertNotNull(response.getPayload());
        assertEquals(0, response.getPayload().size());
    }
}
