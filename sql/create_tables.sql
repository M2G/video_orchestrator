DROP TABLE IF EXISTS video_jobs CASCADE;

CREATE TABLE video_jobs (
                            id BIGSERIAL PRIMARY KEY,
                            filename TEXT UNIQUE NOT NULL,
                            status TEXT NOT NULL,
                            retry_count INT NOT NULL DEFAULT 0,
                            next_retry_at TIMESTAMP NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT now(),
                            updated_at TIMESTAMP NOT NULL DEFAULT now()
)
