package com.example.video_orchestrator.job;

import com.example.video_orchestrator.model.JobResult;
import com.example.video_orchestrator.model.VideoJob;
import org.springframework.stereotype.Component;

@Component
public class DefaultJobHandler implements JobHandler {

    @Override
    public JobResult handle(VideoJob job) {
        return JobResult.SUCCESS;
    }
}
