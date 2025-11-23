package org.game.eternity2.server.redis;

import org.game.eternity2.server.JobManager;
import org.game.eternity2.server.RedisJobQueueAdapter;

/**
 * Factory for creating JobManager instances with different queue
 * implementations.
 */
public class JobManagerFactory {

    /**
     * Create a JobManager with in-memory queue (default).
     * Use for single-server deployments.
     */
    public static JobManager createInMemory() {
        return new JobManager();
    }

    /**
     * Create a JobManager with Redis-backed queue.
     * Use for distributed/multi-server deployments.
     * 
     * @param redisHost Redis server host
     * @param redisPort Redis server port
     * @return JobManager configured with Redis queue
     */
    public static JobManager createWithRedis(String redisHost, int redisPort) {
        RedisConnectionManager redisManager = new RedisConnectionManager(redisHost, redisPort);
        RedisJobQueue redisQueue = new RedisJobQueue(redisManager);
        RedisJobQueueAdapter adapter = new RedisJobQueueAdapter(redisQueue);
        return new JobManager(adapter);
    }

    /**
     * Create a JobManager with Redis-backed queue using default localhost:6379.
     */
    public static JobManager createWithRedis() {
        return createWithRedis("localhost", 6379);
    }
}
