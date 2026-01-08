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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory implementation of JobQueue using ConcurrentLinkedQueue.
 * Thread-safe without explicit synchronization.
 * Default implementation for single-server deployments.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
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
