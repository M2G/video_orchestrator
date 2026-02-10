package com.example.video_orchestrator.repository;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoJobRepositoryContractTest {

    @Test
    void repositoryMustExposeExpectedMethods() {
        Method[] methods =
                VideoJobRepository.class.getDeclaredMethods();

        List<String> names =
                Arrays.stream(methods)
                        .map(Method::getName)
                        .toList();

        assertTrue(names.contains("lockNextJobs"));
        assertTrue(names.contains("markProcessing"));
        assertTrue(names.contains("markDone"));
        assertTrue(names.contains("markRetry"));
        assertTrue(names.contains("markFailed"));
    }
}

