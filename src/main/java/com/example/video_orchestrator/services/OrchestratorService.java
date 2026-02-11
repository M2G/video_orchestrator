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

@Service
public class OrchestratorService {

    private static final Logger log =
            LoggerFactory.getLogger(OrchestratorService.class);

    private final VideoJobRepository repository;
    private final JobHandler jobHandler;
    private final RetryPolicy retryPolicy;
    private final ExecutorService executor;

    @Value("${instance.id}")
    private String instanceId;

    // valeurs simples et explicites
    private static final int MAX_RETRY = 5;
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

    public void runOnce() {
        if (shuttingDown) {
            log.info("Shutdown in progress, skip orchestration");
            return;
        }

        ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;

        int freeSlots =
                pool.getMaximumPoolSize()
                        - pool.getActiveCount()
                        - pool.getQueue().size();

        if (freeSlots <= 0) {
            log.debug("Backpressure: no free executor slots");
            return;
        }

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
                "Orchestration tick: batch={}, active={}, queued={}",
                jobs.size(),
                pool.getActiveCount(),
                pool.getQueue().size()
        );
    }

    private void handleJob(VideoJob job) {
        try {
            jobHandler.handle(job);
            repository.markDone(job.id());

        } catch (Exception e) {
            handleFailure(job, e);
        }
    }

    private void handleFailure(VideoJob job, Exception error) {
        int nextRetry = job.retryCount() + 1;

        if (nextRetry >= MAX_RETRY) {
            repository.markFailed(job.id());
            log.warn("Job {} FAILED after {} retries",
                    job.id(), nextRetry);
            return;
        }

        int delaySeconds =
                retryPolicy.nextDelaySeconds(job.retryCount());

        repository.markRetry(job.id(), delaySeconds);

        log.warn("[{}] Job {} retry {} in {}s",
                instanceId,
                job.id(),
                job.retryCount() + 1,
                delaySeconds);
    }
}
