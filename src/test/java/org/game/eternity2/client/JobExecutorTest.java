package org.game.eternity2.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.game.eternity2.elements.size16x16.EternityBoard16x16;
import org.game.eternity2.elements.Hint;
import org.game.eternity2.server.Job;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JobExecutor.
 *
 * @author Silvere Martin-Michiellot
 * @version 2.0
 */
class JobExecutorTest {

    private JobExecutor executor;
    private ClientStatistics statistics;

    @BeforeEach
    void setUp() {
        statistics = new ClientStatistics();
        executor = new JobExecutor(statistics);
    }

    @Test
    void testExecuteJobNotNull() {
        EternityBoard16x16 board = new EternityBoard16x16();
        List<Job.Position> positions = new ArrayList<>();
        positions.add(new Job.Position(0, 0));

        Job job = new Job("test-job", board, positions, "TestStrategy");

        // Execute should not crash
        assertDoesNotThrow(() -> executor.executeJob(job));
    }

    @Test
    void testStatisticsUpdated() {
        EternityBoard16x16 board = new EternityBoard16x16();
        List<Job.Position> positions = new ArrayList<>();

        Job job = new Job("test-job", board, positions, "TestStrategy");
        executor.executeJob(job);

        assertEquals(1, statistics.getJobsCompleted(), "Should increment jobs completed");
        assertTrue(statistics.getComputeTimeMs() > 0, "Should track compute time");
    }

    @Test
    void testCancel() {
        executor.cancel();
        // Should not crash when cancelled
        assertDoesNotThrow(() -> executor.cancel());
    }
}
