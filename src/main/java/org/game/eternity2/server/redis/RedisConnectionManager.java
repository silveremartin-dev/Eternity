package org.game.eternity2.server.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;

/**
 * Redis connection manager using Lettuce for async operations.
 * Provides connection pooling and async command execution.
 */
public class RedisConnectionManager {
    private static final Logger logger = LogManager.getLogger(RedisConnectionManager.class);

    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisAsyncCommands<String, String> asyncCommands;

    public RedisConnectionManager(String host, int port) {
        RedisURI redisUri = RedisURI.Builder
                .redis(host, port)
                .build();

        this.redisClient = RedisClient.create(redisUri);
        this.connection = redisClient.connect();
        this.asyncCommands = connection.async();

        logger.info("Redis connection established: {}:{}", host, port);
    }

    public RedisConnectionManager() {
        this("localhost", 6379);
    }

    /**
     * Get async commands for non-blocking Redis operations.
     */
    public RedisAsyncCommands<String, String> async() {
        return asyncCommands;
    }

    /**
     * Test the connection with a PING command.
     */
    public boolean ping() {
        try {
            String result = asyncCommands.ping().get();
            return "PONG".equals(result);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Redis PING failed", e);
            return false;
        }
    }

    /**
     * Close the connection and shutdown the client.
     */
    public void shutdown() {
        if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
        logger.info("Redis connection closed");
    }
}
