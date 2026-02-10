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

    private VideoJob toVideoJob(
            Queries.LockAndMarkProcessingRow row
    ) {
        return new VideoJob(
                row.id(),
                row.filename(),
                row.retryCount()
        );
    }

    public void resetStuckJobs() {
        try {
            queries.resetStuckJobs();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<VideoJob> lockAndMarkProcessing(int limit) {
        try {
            return queries.lockAndMarkProcessing(limit)
                    .stream()
                    .map(row -> toVideoJob(row))
                    .toList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

