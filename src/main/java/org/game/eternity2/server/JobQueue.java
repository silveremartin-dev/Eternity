package org.game.eternity2.server;

/**
 * Job queue interface for abstracting queue operations.
 * Allows switching between in-memory and Redis implementations.
 */
public interface JobQueue {
    /**
     * Add a job to the queue.
     */
    void offer(Job job);

    /**
     * Remove and return the next job from the queue.
     * 
     * @return Next job or null if queue is empty
     */
    Job poll();

    /**
     * Clear all jobs from the queue.
     */
    void clear();

    /**
     * Get the current size of the queue.
     * 
     * @return Number of jobs in queue
     */
    int size();
}
