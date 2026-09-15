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

import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * Redis-backed implementation of the JobQueue.
 * Uses a Redis list (LPUSH/RPOP) for persistent task storage.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class RedisJobQueue implements JobQueue, AutoCloseable {

    private static final String QUEUE_KEY = "eternity:jobs:pending";
    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> syncCommands;
    private final Gson gson;

    public RedisJobQueue(String redisUri) {
        this.client = RedisClient.create(redisUri);
        this.connection = client.connect();
        this.syncCommands = connection.sync();
        this.gson = new Gson();
    }

    @Override
    public void offer(Job job) {
        String json = gson.toJson(job);
        syncCommands.lpush(QUEUE_KEY, json);
    }

    @Override
    public Job poll() {
        String json = syncCommands.rpop(QUEUE_KEY);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, Job.class);
    }

    @Override
    public void clear() {
        syncCommands.del(QUEUE_KEY);
    }

    @Override
    public int size() {
        Long len = syncCommands.llen(QUEUE_KEY);
        return len != null ? len.intValue() : 0;
    }

    @Override
    public void close() {
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
        if (client != null) {
            client.shutdown();
        }
    }
}
