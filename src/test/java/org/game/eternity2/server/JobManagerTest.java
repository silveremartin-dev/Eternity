package org.game.eternity2.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.game.eternity2.elements.Hint;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JobManager.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
class JobManagerTest {

    private JobManager jobManager;

    @BeforeEach
    void setUp() {
        jobManager = new JobManager();
    }

    @Test
    void testInitializeJobs() {
        EternityBoard16x16 puzzle = new EternityBoard16x16();
        List<Hint> hints = new ArrayList<>();
        WorkStrategy strategy = new org.game.eternity2.server.strategy.BorderFirstStrategy();

        jobManager.initializeJobs(puzzle, hints, strategy);

        JobManager.JobStatistics stats = jobManager.getStatistics();
        assertTrue(stats.getTotalJobs() > 0, "Should generate jobs");
        assertEquals(stats.getTotalJobs(), stats.pending(), "All jobs should be pending initially");
    }

    @Test
    void testGetNextJob() {
        EternityBoard16x16 puzzle = new EternityBoard16x16();
        List<Hint> hints = new ArrayList<>();
        WorkStrategy strategy = new org.game.eternity2.server.strategy.BorderFirstStrategy();

        jobManager.initializeJobs(puzzle, hints, strategy);

        String clientId = "test-client";
        Job job = jobManager.getNextJob(clientId);

        assertNotNull(job, "Should return a job");
        assertNotNull(job.getJobId(), "Job should have an ID");
        assertNotNull(job.getInitialBoard(), "Job should have a board");
    }

    @Test
    void testJobDeduplication() {
        EternityBoard16x16 puzzle = new EternityBoard16x16();
        List<Hint> hints = new ArrayList<>();
        WorkStrategy strategy = new org.game.eternity2.server.strategy.BorderFirstStrategy();

        jobManager.initializeJobs(puzzle, hints, strategy);

        String clientId = "test-client";
        Job job1 = jobManager.getNextJob(clientId);
        Job job2 = jobManager.getNextJob(clientId);

        assertNotEquals(job1.getJobId(), job2.getJobId(), "Should not return duplicate jobs");
    }

    @Test
    void testStatistics() {
        JobManager.JobStatistics stats = jobManager.getStatistics();

        assertNotNull(stats, "Statistics should not be null");
        assertEquals(0, stats.getTotalJobs(), "Should have 0 jobs initially");
        assertEquals(0, stats.completed(), "Should have 0 completed jobs");
    }
}
