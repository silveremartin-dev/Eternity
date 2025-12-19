/*
 * Copyright 2022-2024 Silvere Martin-Michiellot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.game.eternity2.server;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Job.
 */
class JobTest {

    @Test
    void testJobCreation() {
        Job job = new Job("test-job-1", null, null, "test-strategy");
        assertEquals("test-job-1", job.getJobId());
        assertEquals("test-strategy", job.getStrategyName());
    }

    @Test
    void testJobWithPositions() {
        Job job = new Job("job-2", null, Arrays.asList(
                new Job.Position(0, 0), new Job.Position(0, 1), new Job.Position(1, 0)), "strategy");
        assertNotNull(job.getPositionsToFill());
        assertEquals(3, job.getPositionsToFill().size());
    }

    @Test
    void testJobEquality() {
        Job job1 = new Job("same-id", null, null, "strategy");
        Job job2 = new Job("same-id", null, null, "strategy");
        assertEquals(job1.getJobId(), job2.getJobId());
    }

    @Test
    void testPositionEquality() {
        Job.Position p1 = new Job.Position(3, 5);
        Job.Position p2 = new Job.Position(3, 5);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void testPositionToString() {
        Job.Position p = new Job.Position(2, 4);
        assertEquals("(2,4)", p.toString());
    }

    @Test
    void testJobCreatedTimestamp() {
        long before = System.currentTimeMillis();
        Job job = new Job("time-test", null, null, "strategy");
        long after = System.currentTimeMillis();

        assertTrue(job.getCreatedTimestamp() >= before);
        assertTrue(job.getCreatedTimestamp() <= after);
    }
}
