DROP TABLE IF EXISTS video_jobs CASCADE;

CREATE TABLE video_jobs (
                            id BIGSERIAL PRIMARY KEY,
                            filename VARCHAR(255) NOT NULL UNIQUE,
                            status VARCHAR(50) NOT NULL,
                            retryCount INTEGER NOT NULL DEFAULT 0,
                            createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updatedAt TIMESTAMP
);