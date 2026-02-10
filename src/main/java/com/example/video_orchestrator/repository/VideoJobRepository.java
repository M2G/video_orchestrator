package com.example.video_orchestrator.repository;

import com.example.postgresql.Queries;
import com.example.video_orchestrator.model.VideoJob;

import java.util.List;

public interface VideoJobRepository {
    void markProcessing(long id);
    void markDone(long id);
    void markRetry(long id, int delaySeconds);
    void markFailed(long id);
    List<VideoJob> lockAndMarkProcessing(int limit);
}