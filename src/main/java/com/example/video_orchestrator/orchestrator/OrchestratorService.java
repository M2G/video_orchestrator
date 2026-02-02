package com.example.video_orchestrator.orchestrator;

import com.example.postgresql.Queries;
import com.example.video_orchestrator.job.JobHandler;
import com.example.video_orchestrator.job.VideoJob;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class OrchestratorService {

    private final Queries queries;
    private final ExecutorService executor;
    private final JobHandler jobHandler;

    private static final int MAX_RETRY = 5;
    private static final int BATCH_SIZE = 5;

    public OrchestratorService(
            Queries queries,
            ExecutorService executor,
            JobHandler jobHandler
    ) {
        this.queries = queries;
        this.executor = executor;
        this.jobHandler = jobHandler;
    }

    public void runOnce() throws SQLException {
        List<VideoJob> jobs =
                queries.lockNextJobs(MAX_RETRY, BATCH_SIZE)
                        .stream()
                        .map(VideoJob::from)
                        .toList();

        for (VideoJob job : jobs) {
            executor.submit(() -> {
                try {
                    handle(job);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void handle(VideoJob job) throws SQLException {
        try {
            queries.markProcessing((int) job.id());
            jobHandler.process(job.filename());
            queries.markDone((int) job.id());
        } catch (Exception e) {
            handleRetry(job);
        }
    }

    private void handleRetry(VideoJob job) throws SQLException {
        if (job.retryCount() + 1 >= MAX_RETRY) {
            queries.markFailed((int) job.id());
            return;
        }

        LocalDateTime nextRetry =
                LocalDateTime.from(Instant.now().plus(backoffSeconds(job.retryCount()), ChronoUnit.SECONDS));

        queries.markRetry(nextRetry, (int) job.id());
    }

    private long backoffSeconds(int retryCount) {
        return (long) Math.pow(2, retryCount + 1); // 2,4,8,16...
    }
}
