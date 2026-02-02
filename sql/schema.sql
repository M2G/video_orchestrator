CREATE TABLE IF NOT EXISTS video_jobs (
                                          id SERIAL PRIMARY KEY,
                                          filename TEXT NOT NULL UNIQUE,
                                          status TEXT NOT NULL,
                                          retry_count INT NOT NULL DEFAULT 0,
                                          next_retry_at TIMESTAMP NULL,
                                          created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS orchestrator_lock (
                                                 id INT PRIMARY KEY,
                                                 locked_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_video_jobs_status
    ON video_jobs(status);

CREATE INDEX IF NOT EXISTS idx_video_jobs_retry
    ON video_jobs(status, next_retry_at);