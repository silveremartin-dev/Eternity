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

import org.game.eternity2.server.JobManager;
import org.game.eternity2.server.RedisJobQueueAdapter;

/**
 * Factory for creating JobManager instances with different queue
 * implementations.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
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
