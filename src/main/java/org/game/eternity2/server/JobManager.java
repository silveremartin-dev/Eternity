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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.game.eternity2.model.BoardPrimitive;
import org.game.eternity2.model.Hint;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages job creation, distribution, and tracking.
 * Ensures no duplicate jobs are dispatched and handles job lifecycle.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
  * @author Antigravity
  * @since 1.0
 */
public class JobManager {
    private static final Logger logger = LogManager.getLogger(JobManager.class);

    private final Map<String, JobStatus> jobStatuses;
    private final JobQueue pendingJobs;
    @SuppressWarnings("unused")
    private WorkStrategy currentStrategy;

    /**
     * Default constructor using in-memory queue.
     */
    public JobManager() {
        this(new InMemoryJobQueue());
    }

    /**
     * Constructor allowing custom queue implementation (e.g., Redis).
     * 
     * @param jobQueue Queue implementation to use
     */
    public JobManager(JobQueue jobQueue) {
        this.jobStatuses = new ConcurrentHashMap<>();
        this.pendingJobs = jobQueue;
    }

    /**
     * Initialize jobs for a puzzle using the specified strategy.
     *
     * @param puzzle   The puzzle to solve
     * @param hints    Pre-placed tiles
     * @param strategy Work distribution strategy
     */
    public void initializeJobs(BoardPrimitive puzzle, List<Hint> hints, WorkStrategy strategy) {
        this.currentStrategy = strategy;
        List<Job> jobs = strategy.generateJobs(puzzle, hints);

        pendingJobs.clear();
        jobStatuses.clear();

        long currentSeed = 42L;
        for (Job job : jobs) {
            Job seedJob = new Job(job.getJobId(), job.getInitialBoard(), job.getPositionsToFill(), job.getStrategyName(), currentSeed++);
            pendingJobs.offer(seedJob);
            jobStatuses.put(seedJob.getJobId(), new JobStatus(seedJob));
        }

        logger.info("Initialized {} jobs with deterministic seeds using strategy: {}", jobs.size(), strategy.getName());
    }

    /**
     * Get the next available job for a client.
     *
     * @param clientId ID of the requesting client
     * @return Next job or null if none available
     */
    public Job getNextJob(String clientId) {
        Job job = pendingJobs.poll();
        if (job != null) {
            JobStatus status = jobStatuses.get(job.getJobId());
            status.markDispatched(clientId);
            logger.info("Dispatched job {} to client {}", job.getJobId(), clientId);
        }
        return job;
    }

    /**
     * Mark a job as completed.
     *
     * @param jobId  ID of the completed job
     * @param result Result board (may be null if no solution found)
     */
    public void markJobCompleted(String jobId, BoardPrimitive result) {
        JobStatus status = jobStatuses.get(jobId);
        if (status != null) {
            status.markCompleted(result);
            logger.info("Job {} completed", jobId);
        }
    }

    /**
     * Mark a job as failed and re-queue it.
     *
     * @param jobId ID of the failed job
     */
    public void markJobFailed(String jobId) {
        JobStatus status = jobStatuses.get(jobId);
        if (status != null) {
            status.markFailed();
            pendingJobs.offer(status.getJob());
            logger.warn("Job {} failed, re-queued", jobId);
        }
    }

    /**
     * Mark the active job assigned to the given client as completed.
     *
     * @param clientId ID of the client
     * @param result Result board
     */
    public void markClientJobCompleted(String clientId, BoardPrimitive result) {
        for (JobStatus status : jobStatuses.values()) {
            if (status.getState() == JobState.DISPATCHED && clientId.equals(status.assignedClientId)) {
                status.markCompleted(result);
                logger.info("Job {} completed by client {}", status.getJob().getJobId(), clientId);
                return;
            }
        }
        logger.warn("Received result from client {} but no active dispatched job was found", clientId);
    }

    /**
     * Mark the active job assigned to the given client as failed and re-queue it.
     *
     * @param clientId ID of the client
     */
    public void markClientJobFailed(String clientId) {
        for (JobStatus status : jobStatuses.values()) {
            if (status.getState() == JobState.DISPATCHED && clientId.equals(status.assignedClientId)) {
                status.markFailed();
                pendingJobs.offer(status.getJob());
                logger.warn("Job {} failed (client {} disconnected), re-queued", status.getJob().getJobId(), clientId);
                return;
            }
        }
    }

    /**
     * Get statistics about job progress.
     *
     * @return Job statistics
     */
    public JobStatistics getStatistics() {
        int total = jobStatuses.size();
        int completed = 0;
        int dispatched = 0;
        int pending = 0;
        int failed = 0;

        for (JobStatus status : jobStatuses.values()) {
            switch (status.getState()) {
                case COMPLETED -> completed++;
                case DISPATCHED -> dispatched++;
                case PENDING -> pending++;
                case FAILED -> failed++;
            }
        }

        return new JobStatistics(total, completed, dispatched, pending, failed);
    }

    /**
     * Job status tracking.
     */
    private static class JobStatus {
        private final Job job;
        private JobState state;
        @SuppressWarnings("unused")
        private String assignedClientId;
        @SuppressWarnings("unused")
        private long dispatchedTimestamp;
        @SuppressWarnings("unused")
        private BoardPrimitive result;

        public JobStatus(Job job) {
            this.job = job;
            this.state = JobState.PENDING;
        }

        public void markDispatched(String clientId) {
            this.state = JobState.DISPATCHED;
            this.assignedClientId = clientId;
            this.dispatchedTimestamp = System.currentTimeMillis();
        }

        public void markCompleted(BoardPrimitive result) {
            this.state = JobState.COMPLETED;
            this.result = result;
        }

        public void markFailed() {
            this.state = JobState.PENDING;
            this.assignedClientId = null;
        }

        public JobState getState() {
            return state;
        }

        public Job getJob() {
            return job;
        }
    }

    private enum JobState {
        PENDING, DISPATCHED, COMPLETED, FAILED
    }

    /**
     * Job statistics.
     */
    public record JobStatistics(int total, int completed, int dispatched, int pending, int failed) {
        public double getCompletionPercentage() {
            return total > 0 ? (completed * 100.0 / total) : 0.0;
        }

        public int getTotalJobs() {
            return total;
        }
    }
}
