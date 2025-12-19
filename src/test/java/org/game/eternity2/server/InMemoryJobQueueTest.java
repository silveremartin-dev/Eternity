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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryJobQueue.
 */
class InMemoryJobQueueTest {

    private InMemoryJobQueue queue;

    @BeforeEach
    void setUp() {
        queue = new InMemoryJobQueue();
    }

    @Test
    void testOfferAndPoll() {
        Job job = new Job("job1", null, null, "test-strategy");
        queue.offer(job);
        assertEquals(1, queue.size());

        Job polled = queue.poll();
        assertEquals("job1", polled.getJobId());
        assertEquals(0, queue.size());
    }

    @Test
    void testPollEmptyQueue() {
        assertNull(queue.poll(), "Poll on empty queue should return null");
    }

    @Test
    void testFIFOOrder() {
        queue.offer(new Job("first", null, null, "strategy"));
        queue.offer(new Job("second", null, null, "strategy"));
        queue.offer(new Job("third", null, null, "strategy"));

        assertEquals("first", queue.poll().getJobId());
        assertEquals("second", queue.poll().getJobId());
        assertEquals("third", queue.poll().getJobId());
    }

    @Test
    void testClear() {
        queue.offer(new Job("job1", null, null, "strategy"));
        queue.offer(new Job("job2", null, null, "strategy"));
        assertEquals(2, queue.size());

        queue.clear();
        assertEquals(0, queue.size());
        assertNull(queue.poll());
    }

    @Test
    void testSizeTracking() {
        assertEquals(0, queue.size());

        queue.offer(new Job("a", null, null, "strategy"));
        assertEquals(1, queue.size());

        queue.offer(new Job("b", null, null, "strategy"));
        assertEquals(2, queue.size());

        queue.poll();
        assertEquals(1, queue.size());
    }
}
