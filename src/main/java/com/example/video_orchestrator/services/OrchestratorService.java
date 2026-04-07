package com.example.video_orchestrator.services;

import com.example.video_orchestrator.retry.RetryPolicy;
import com.example.video_orchestrator.job.JobHandler;
import com.example.video_orchestrator.model.VideoJob;
import com.example.video_orchestrator.repository.VideoJobRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Service
public class OrchestratorService {
    // Logger monitoring
    private static final Logger log =
            LoggerFactory.getLogger(OrchestratorService.class);
    // Access DB (sqlc)
    private final VideoJobRepository repository;
    // Traitement métier du job
    private final JobHandler jobHandler;
    // Stratégie de retry
    private final RetryPolicy retryPolicy;
    // Pool de threads (parallélisme)
    private final ExecutorService executor;
    // ID de l’instance (multi-node)
    @Value("${instance.id}")
    private String instanceId;
    // ID de l’instance (multi-node)
    private static final int MAX_RETRY = 5;
    // Taille max d’un batch
    private static final int MAX_BATCH = 10;

    public OrchestratorService(
            VideoJobRepository repository,
            JobHandler jobHandler,
            RetryPolicy retryPolicy,
            ExecutorService executor
    ) {
        this.repository = repository;
        this.jobHandler = jobHandler;
        this.retryPolicy = retryPolicy;
        this.executor = executor;
    }

    private volatile boolean shuttingDown = false;

    @PreDestroy
    public void onShutdown() {
        shuttingDown = true;
        log.info("Graceful shutdown started");
    }

    // lance un cycle d’orchestration
    public void runOnce() {
        if (shuttingDown) {
            log.info("Shutdown in progress, skip orchestration");
            return;
        }

        ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
        // Nombre de slots disponibles
        int freeSlots =
                pool.getMaximumPoolSize()
                        - pool.getActiveCount()
                        - pool.getQueue().size();

        // Backpressure : rien à faire si saturé
        if (freeSlots <= 0) {
            log.debug("Backpressure: no free executor slots");
            return;
        }
        // Backpressure : rien à faire si saturé
        int batchSize = Math.min(freeSlots, MAX_BATCH);

        // SQL: lock + mark PROCESSING (sqlc safe)
        List<VideoJob> jobs =
                repository.lockAndMarkProcessing(batchSize);

        // filtrage retry côté Java (limitation sqlc)
        jobs.stream()
                .filter(job -> job.retryCount() < MAX_RETRY)
                .forEach(job ->
                        executor.submit(() -> handleJob(job))
                );

        log.info(
                "event=orchestrator_tick instance={} active={} queued={}",
                instanceId,
                pool.getActiveCount(),
                pool.getQueue().size()
        );
    }
    // Traite un job
    private void handleJob(VideoJob job) {

        MDC.put("instance", instanceId);
        MDC.put("jobId", String.valueOf(job.id()));

        try {
            long start = System.currentTimeMillis();
            // Traitement métier
            jobHandler.handle(job);

            repository.markDone(job.id());

            long duration = System.currentTimeMillis() - start;

            log.info(
                    "event=job_completed instance={} jobId={} durationMs={}",
                    instanceId,
                    job.id(),
                    duration
            );

        } catch (Exception e) {
            handleFailure(job, e);
        } finally {
            MDC.clear();
        }
    }
    // Gère un échec
    private void handleFailure(VideoJob job, Exception error) {
        int nextRetry = job.retryCount() + 1;

        // Trop de retries -> FAILED
        if (nextRetry >= MAX_RETRY) {
            repository.markFailed(job.id());
            log.error(
                    "event=job_failed instance={} jobId={} retry={}",
                    instanceId,
                    job.id(),
                    nextRetry
            );
            log.error("e: ", error); // @TODO refactor
            return;
        }

        // Calcul du délai de retry
        int delaySeconds =
                retryPolicy.nextDelaySeconds(job.retryCount());

        // Calcul du délai de retry
        repository.markRetry(job.id(), delaySeconds);

        log.error(
                "event=job_failed instance={} jobId={} retry={}",
                instanceId,
                job.id(),
                nextRetry
        );
    }
}
