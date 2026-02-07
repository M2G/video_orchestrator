package com.example.video_orchestrator.services;

import com.example.video_orchestrator.retry.RetryPolicy;
import com.example.video_orchestrator.job.JobHandler;
import com.example.video_orchestrator.model.VideoJob;
import com.example.video_orchestrator.repository.VideoJobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class OrchestratorService {

    private final VideoJobRepository repository;
    private final JobHandler jobHandler;
    private final RetryPolicy retryPolicy;
    private final ExecutorService executor;

    private final int maxRetry;

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
        this.maxRetry = 5; // ou @Value
    }

    public void runOnce(int batchSize) {
        List<VideoJob> jobs =
                repository.lockNextJobs(batchSize, maxRetry);

        for (VideoJob job : jobs) {
            executor.submit(() -> processJob(job));
        }
    }

    private void processJob(VideoJob job) {
        try {
            repository.markProcessing(job.id());

            jobHandler.handle(job);

            repository.markDone(job.id());

        } catch (Exception e) {
            int delaySeconds =
                    retryPolicy.nextDelaySeconds(job.retryCount());

            repository.markRetry(delaySeconds, (int) job.id());
        }
    }
}
