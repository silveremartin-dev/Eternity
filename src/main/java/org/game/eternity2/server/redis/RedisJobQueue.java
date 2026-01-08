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
package org.game.eternity2.server.redis;

import com.google.gson.Gson;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.server.Job;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Redis-backed job queue using LPUSH/BRPOP for distributed job management.
 * Provides persistent, distributed queue for job distribution across multiple
 * servers.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class RedisJobQueue {
    private static final Logger logger = LogManager.getLogger(RedisJobQueue.class);
    private static final String QUEUE_KEY = "eternity:jobs:pending";

    @SuppressWarnings("unused")
    private final RedisConnectionManager redisManager;
    private final RedisAsyncCommands<String, String> async;
    private final Gson gson;

    public RedisJobQueue(RedisConnectionManager redisManager) {
        this.redisManager = redisManager;
        this.async = redisManager.async();
        this.gson = new Gson();
    }

    /**
     * Add a job to the queue (LPUSH - left push).
     * Jobs are pushed to the left, popped from the right (FIFO).
     * 
     * @param job Job to enqueue
     * @return true if successful
     */
    public boolean enqueue(Job job) {
        try {
            String jobJson = gson.toJson(job);
            Long result = async.lpush(QUEUE_KEY, jobJson).get(5, TimeUnit.SECONDS);
            logger.debug("Enqueued job {} to Redis, queue size: {}", job.getJobId(), result);
            return result != null && result > 0;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Failed to enqueue job " + job.getJobId(), e);
            return false;
        }
    }

    /**
     * Dequeue a job from the queue (BRPOP - blocking right pop).
     * Blocks for up to timeout seconds if queue is empty.
     * 
     * @param timeoutSeconds Maximum time to wait for a job
     * @return Next job or null if timeout
     */
    public Job dequeue(int timeoutSeconds) {
        try {
            var result = async.brpop(timeoutSeconds, QUEUE_KEY).get(timeoutSeconds + 2L, TimeUnit.SECONDS);

            if (result != null && result.hasValue()) {
                String jobJson = result.getValue();
                Job job = gson.fromJson(jobJson, Job.class);
                logger.debug("Dequeued job {} from Redis", job.getJobId());
                return job;
            }

            return null; // Timeout or empty queue
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Failed to dequeue job from Redis", e);
            return null;
        }
    }

    /**
     * Get the current queue size (non-blocking).
     * 
     * @return Number of pending jobs in queue
     */
    public long size() {
        try {
            Long len = async.llen(QUEUE_KEY).get(2, TimeUnit.SECONDS);
            return len != null ? len : 0;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Failed to get queue size", e);
            return 0;
        }
    }

    /**
     * Clear all jobs from the queue.
     * USE WITH CAUTION - this deletes all pending jobs!
     */
    public void clear() {
        try {
            async.del(QUEUE_KEY).get(2, TimeUnit.SECONDS);
            logger.warn("Cleared job queue in Redis");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Failed to clear queue", e);
        }
    }
}
