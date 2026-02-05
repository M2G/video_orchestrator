package com.example.video_orchestrator.orchestrator;

import com.example.video_orchestrator.model.JobResult;
import com.example.video_orchestrator.repository.SqlcVideoJobRepository;
import com.example.video_orchestrator.job.JobHandler;
import com.example.video_orchestrator.model.VideoJob;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class OrchestratorService {

    private final SqlcVideoJobRepository repository;
    private final JobHandler jobHandler;
    private final ExecutorService executor;
    private final RetryPolicy retryPolicy;

    public OrchestratorService(
            SqlcVideoJobRepository repository,
            JobHandler jobHandler,
            ExecutorService executor,
            RetryPolicy retryPolicy
    ) {
        this.repository = repository;
        this.jobHandler = jobHandler;
        this.executor = executor;
        this.retryPolicy = retryPolicy;
    }

    public void runOnce() throws SQLException {
        List<VideoJob> jobs =
                repository.lockNextJobs(10, retryPolicy.maxRetry());

        for (VideoJob job : jobs) {
            repository.markProcessing((int) job.id());

            executor.submit(() -> handleJob(job));
        }
    }

    private void handleJob(VideoJob job) {
        try {
            JobResult result = jobHandler.handle(job);

            switch (result) {
                case SUCCESS -> repository.markDone((int) job.id());
                case RETRY -> repository.markRetry(
                        retryPolicy.nextDelay(job.retryCount()), (int) job.id());
                case FAILED -> repository.markFailed((int) job.id());
            }

        } catch (Exception e) {
            try {
                repository.markFailed((int) job.id());
            } catch (SQLException ignored) {}
        }
    }
}

