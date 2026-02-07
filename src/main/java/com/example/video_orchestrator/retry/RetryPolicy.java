package com.example.video_orchestrator.retry;

import org.springframework.stereotype.Component;

@Component
public class RetryPolicy {

    public int nextDelaySeconds(int retryCount) {
        return Math.min((int) Math.pow(2, retryCount) * 5, 300);
    }
}