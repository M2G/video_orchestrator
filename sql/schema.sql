CREATE TABLE video_jobs (
                            id SERIAL PRIMARY KEY,
                            filename TEXT UNIQUE NOT NULL,
                            status TEXT NOT NULL,
                            retry_count INT NOT NULL DEFAULT 0,
                            last_error TEXT,
                            created_at TIMESTAMP NOT NULL DEFAULT now(),
                            updated_at TIMESTAMP NOT NULL DEFAULT now()
);
