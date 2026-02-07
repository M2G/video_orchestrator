package com.example.video_orchestrator.orchestrator;

import com.example.video_orchestrator.retry.RetryPolicy;
import com.example.video_orchestrator.services.OrchestratorService;
import com.example.video_orchestrator.repository.VideoJobRepository;
import com.example.video_orchestrator.job.JobHandler;
import com.example.video_orchestrator.model.VideoJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrchestratorServiceTest {

    @Mock
    VideoJobRepository repository;

    @Mock
    JobHandler jobHandler;

    @Mock
    RetryPolicy retryPolicy;

    ExecutorService executor;

    OrchestratorService service;

    @BeforeEach
    void setup() {
        executor = Executors.newSingleThreadExecutor();
        service = new OrchestratorService(
                repository,
                jobHandler,
                retryPolicy,
                executor
        );
    }

    @Test
    void shouldProcessJobSuccessfully() throws Exception {
        VideoJob job = new VideoJob(1L, "video.mp4", 0);

        when(repository.lockNextJobs(anyInt(), anyInt()))
                .thenReturn(List.of(job));

        service.runOnce(1);

        verify(repository).markProcessing(1L);
        verify(jobHandler).handle(job);
        verify(repository).markDone(1L);
    }

    @Test
    void shouldRetryOnFailure() throws Exception {
        VideoJob job = new VideoJob(1L, "video.mp4", 1);

        when(repository.lockNextJobs(anyInt(), anyInt()))
                .thenReturn(List.of(job));

        doThrow(new RuntimeException("fail"))
                .when(jobHandler).handle(job);

        when(retryPolicy.nextDelaySeconds(1)).thenReturn(10);

        service.runOnce(1);

        verify(repository).markRetry(10L, 1);
    }
}


