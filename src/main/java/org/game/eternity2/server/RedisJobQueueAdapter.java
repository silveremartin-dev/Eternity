package org.game.eternity2.server;

import org.game.eternity2.server.redis.RedisJobQueue;

/**
 * Adapter to make RedisJobQueue compatible with JobQueue interface.
 */
public class RedisJobQueueAdapter implements JobQueue {
    private final RedisJobQueue redisQueue;

    public RedisJobQueueAdapter(RedisJobQueue redisQueue) {
        this.redisQueue = redisQueue;
    }

    @Override
    public void offer(Job job) {
        redisQueue.enqueue(job);
    }

    @Override
    public Job poll() {
        // Use 1 second timeout for BRPOP
        return redisQueue.dequeue(1);
    }

    @Override
    public void clear() {
        redisQueue.clear();
    }

    @Override
    public int size() {
        return (int) redisQueue.size();
    }
}
