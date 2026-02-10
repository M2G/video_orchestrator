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

-- Jobs à traiter
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_video_jobs_pending
    ON video_jobs (status, retry_count, created_at);

-- Retry différé
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_video_jobs_next_retry
    ON video_jobs (next_retry_at);

-- Sécurité lock
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_video_jobs_id_status
    ON video_jobs (id, status);

CREATE INDEX IF NOT EXISTS idx_video_jobs_pending
    ON video_jobs (status, retry_count, created_at);

CREATE INDEX IF NOT EXISTS idx_video_jobs_next_retry
    ON video_jobs (next_retry_at);

CREATE INDEX idx_video_jobs_pending
    ON video_jobs (status, retry_count, created_at);