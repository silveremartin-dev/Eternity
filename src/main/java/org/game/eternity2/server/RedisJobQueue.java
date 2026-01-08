package org.game.eternity2.server;

import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * Redis-backed implementation of the JobQueue.
 * Uses a Redis list (LPUSH/RPOP) for persistent task storage.
 */
public class RedisJobQueue implements JobQueue {

    private static final String QUEUE_KEY = "eternity:jobs:pending";
    private final RedisCommands<String, String> syncCommands;
    private final Gson gson;

    public RedisJobQueue(String redisUri) {
        RedisClient client = RedisClient.create(redisUri);
        StatefulRedisConnection<String, String> connection = client.connect();
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
}
