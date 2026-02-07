package com.example.video_orchestrator.repository;

import com.example.postgresql.Queries;
import com.example.video_orchestrator.model.VideoJob;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlcVideoJobRepository implements VideoJobRepository {

    private final Queries queries;

    public SqlcVideoJobRepository(Queries queries) {
        this.queries = queries;
    }

    public List<VideoJob> lockNextJobs(int limit, int maxRetry) {
        try {
            return queries.lockNextJobs(limit, maxRetry)
                    .stream()
                    .map(this::toVideoJob)
                    .toList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void markProcessing(long id) {
        try {
            queries.markProcessing((int) id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void markDone(long id) {
        try {
            queries.markDone((int) id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void markRetry(long id, int delaySeconds) {
        try {
            queries.markRetry(delaySeconds, (int) id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void markFailed(long id) {
        try {
            queries.markFailed((int) id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private VideoJob toVideoJob(Queries.LockNextJobsRow row) {
        return new VideoJob(
                row.id(),
                row.filename(),
                row.retryCount()
        );
    }
}

