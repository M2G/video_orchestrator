package com.example.video_orchestrator.job;

import com.example.video_orchestrator.model.JobResult;
import com.example.video_orchestrator.model.VideoJob;

public interface JobHandler {
    JobResult handle(VideoJob job) throws Exception;
}
