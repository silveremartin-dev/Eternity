package org.game.eternity2.server.monitoring;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.*;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics provider for the Eternity server.
 * Exposes metrics in Prometheus format via /metrics endpoint.
 */
public class MetricsProvider {

    private static final Logger LOGGER = LogManager.getLogger(MetricsProvider.class);
    private static MetricsProvider instance;

    private final PrometheusMeterRegistry registry;

    // Counters
    private final Counter jobsSubmitted;
    private final Counter jobsCompleted;
    private final Counter candidatesChecked;
    private final Counter piecesPlaced;

    // Gauges
    private final AtomicLong activeClients = new AtomicLong(0);
    private final AtomicLong queueDepth = new AtomicLong(0);

    // Timers
    private final Timer jobExecutionTime;
    private final Timer kernelExecutionTime;

    private MetricsProvider() {
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // Register JVM metrics
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new UptimeMetrics().bindTo(registry);

        // Custom counters
        jobsSubmitted = Counter.builder("eternity_jobs_submitted_total")
                .description("Total number of jobs submitted")
                .register(registry);

        jobsCompleted = Counter.builder("eternity_jobs_completed_total")
                .description("Total number of jobs completed")
                .register(registry);

        candidatesChecked = Counter.builder("eternity_candidates_checked_total")
                .description("Total number of candidates checked by kernel")
                .register(registry);

        piecesPlaced = Counter.builder("eternity_pieces_placed_total")
                .description("Total number of pieces placed")
                .register(registry);

        // Custom gauges
        Gauge.builder("eternity_active_clients", activeClients, AtomicLong::get)
                .description("Number of active clients")
                .register(registry);

        Gauge.builder("eternity_queue_depth", queueDepth, AtomicLong::get)
                .description("Number of jobs in queue")
                .register(registry);

        // Custom timers
        jobExecutionTime = Timer.builder("eternity_job_execution_seconds")
                .description("Job execution time in seconds")
                .register(registry);

        kernelExecutionTime = Timer.builder("eternity_kernel_execution_seconds")
                .description("Kernel execution time in seconds")
                .register(registry);

        LOGGER.info("Metrics provider initialized");
    }

    public static synchronized MetricsProvider getInstance() {
        if (instance == null) {
            instance = new MetricsProvider();
        }
        return instance;
    }

    /**
     * Get Prometheus-formatted metrics output.
     */
    public String scrape() {
        return registry.scrape();
    }

    /**
     * Increment jobs submitted counter.
     */
    public void recordJobSubmitted() {
        jobsSubmitted.increment();
    }

    /**
     * Increment jobs completed counter.
     */
    public void recordJobCompleted() {
        jobsCompleted.increment();
    }

    /**
     * Record candidates checked.
     */
    public void recordCandidatesChecked(long count) {
        candidatesChecked.increment(count);
    }

    /**
     * Record piece placed.
     */
    public void recordPiecePlaced() {
        piecesPlaced.increment();
    }

    /**
     * Update active clients count.
     */
    public void setActiveClients(long count) {
        activeClients.set(count);
    }

    /**
     * Update queue depth.
     */
    public void setQueueDepth(long depth) {
        queueDepth.set(depth);
    }

    /**
     * Record job execution time.
     */
    public Timer.Sample startJobTimer() {
        return Timer.start(registry);
    }

    public void stopJobTimer(Timer.Sample sample) {
        sample.stop(jobExecutionTime);
    }

    /**
     * Record kernel execution time.
     */
    public Timer.Sample startKernelTimer() {
        return Timer.start(registry);
    }

    public void stopKernelTimer(Timer.Sample sample) {
        sample.stop(kernelExecutionTime);
    }

    /**
     * Get the underlying registry.
     */
    public MeterRegistry getRegistry() {
        return registry;
    }
}
