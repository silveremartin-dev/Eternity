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
package org.game.eternity2.server;

import org.game.eternity2.server.redis.RedisJobQueue;

/**
 * Adapter to make RedisJobQueue compatible with JobQueue interface.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
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
