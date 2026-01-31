package com.example.video_orchestrator.orchestrator;

import com.example.postgresql.Queries;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class OrchestratorService {

    private final Queries queries;
    private final ExecutorService executor;

    public OrchestratorService(Queries queries, ExecutorService executor) {
        this.queries = queries;
        this.executor = executor;
    }

    /**
     * Une exécution = un cycle d’orchestration (cron-friendly)
     */
    public void runOnce() throws Exception {
        var jobs = queries.findRunnableJobs();

        for (var job : jobs) {
            executor.submit(() -> handle(job.filename(), job.retryCount()));
        }
    }

    /**
     * Traitement d’un job unique
     */
    private void handle(String filename, int retryCount) {
        try {
            // Lock pessimiste via DB
            int updated = queries.lockJob(filename);
            if (updated == 0) {
                // déjà pris par un autre worker
                return;
            }

            System.out.println("Processing file: " + filename);

            Thread.sleep(1000); // simulation

            // ok
            queries.upsertVideoJob(filename, "DONE");

        } catch (Exception e) {
            try {
                // Retry
                queries.incrementRetry(filename);

                if (retryCount + 1 >= RetryPolicy.MAX_RETRIES) {
                    queries.upsertVideoJob(filename, "ERROR");
                } else {
                    queries.upsertVideoJob(filename, "PENDING");
                }

            } catch (Exception fatal) {
                throw new RuntimeException(fatal);
            }
        }
    }
}
