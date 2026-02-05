package com.example.video_orchestrator.repository;

import com.example.postgresql.Queries;
import com.example.video_orchestrator.model.VideoJob;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlcVideoJobRepository {

    private final Queries queries;

    public SqlcVideoJobRepository(Queries queries) {
        this.queries = queries;
    }

    public List<VideoJob> lockNextJobs(int limit, int maxRetry) throws SQLException {
        return queries.lockNextJobs(limit, maxRetry)
                .stream()
                .map(r -> new VideoJob(r.id(), r.filename(), r.retryCount()))
                .toList();
    }

    public void markProcessing(int id) throws SQLException {
        queries.markProcessing(id);
    }

    public void markRetry(int delaySeconds, int id) throws SQLException {
        queries.markRetry(delaySeconds, id);
    }

    public void markFailed(int id) throws SQLException {
        queries.markFailed(id);
    }

    public void markDone(int id) throws SQLException {
        queries.markDone(id);
    }
}
