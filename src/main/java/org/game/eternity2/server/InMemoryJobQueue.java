package org.game.eternity2.server;

import java.util.LinkedList;
import java.util.Queue;

/**
 * In-memory implementation of JobQueue using LinkedList.
 * Default implementation for single-server deployments.
 */
public class InMemoryJobQueue implements JobQueue {
    private final Queue<Job> queue = new LinkedList<>();

    @Override
    public synchronized void offer(Job job) {
        queue.offer(job);
    }

    @Override
    public synchronized Job poll() {
        return queue.poll();
    }

    @Override
    public synchronized void clear() {
        queue.clear();
    }

    @Override
    public synchronized int size() {
        return queue.size();
    }
}
