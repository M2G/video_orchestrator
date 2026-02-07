package com.example.video_orchestrator.retry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryPolicyTest {

    @Test
    void shouldIncreaseDelayExponentially() {
        RetryPolicy policy = new RetryPolicy();

        int d1 = policy.nextDelaySeconds(0);
        int d2 = policy.nextDelaySeconds(1);
        int d3 = policy.nextDelaySeconds(2);

        assertTrue(d2 > d1);
        assertTrue(d3 > d2);
    }
}
