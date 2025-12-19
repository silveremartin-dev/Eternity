package org.game.eternity2.server;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of JobQueue using ConcurrentLinkedQueue.
 * Thread-safe without explicit synchronization.
 * Default implementation for single-server deployments.
 */
public class InMemoryJobQueue implements JobQueue {
    private final ConcurrentLinkedQueue<Job> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger size = new AtomicInteger(0);

    @Override
    public void offer(Job job) {
        queue.offer(job);
        size.incrementAndGet();
    }

    @Override
    public Job poll() {
        Job job = queue.poll();
        if (job != null) {
            size.decrementAndGet();
        }
        return job;
    }

    @Override
    public void clear() {
        queue.clear();
        size.set(0);
    }

    @Override
    public int size() {
        return size.get();
    }
}
