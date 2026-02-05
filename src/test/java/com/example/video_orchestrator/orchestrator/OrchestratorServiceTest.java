package com.example.video_orchestrator.orchestrator;

import com.example.video_orchestrator.repository.VideoJobRepository;
import com.example.video_orchestrator.job.JobHandler;
import com.example.video_orchestrator.model.VideoJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class OrchestratorServiceTest {

    private VideoJobRepository repository;
    private JobHandler jobHandler;
    private ExecutorService executor;
    private OrchestratorService service;

    @BeforeEach
    void setup() {
        repository = mock(VideoJobRepository.class);
        jobHandler = mock(JobHandler.class);
        executor = Executors.newSingleThreadExecutor();

        service = new OrchestratorService(
                repository,
                jobHandler,
                executor
        );
    }

    @Test
    void shouldProcessOneJob() throws Exception {
        VideoJob job = new VideoJob(1L, "video.mp4", 0);

        when(repository.lockNextJobs(anyInt(), anyInt()))
                .thenReturn(List.of(job));

        service.runOnce();

        verify(repository).markProcessing(1L);
        verify(jobHandler).handle(job);
    }
}

