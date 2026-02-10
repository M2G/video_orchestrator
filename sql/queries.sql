-- name: UpsertVideoJob :exec
INSERT INTO video_jobs (filename, status)
VALUES ($1, $2)
    ON CONFLICT (filename)
DO UPDATE SET
    status = EXCLUDED.status,
           updated_at = now();

-- name: MarkProcessing :exec
UPDATE video_jobs
SET status = 'PROCESSING',
    updated_at = now()
WHERE id = $1;

-- name: MarkFailed :exec
UPDATE video_jobs
SET status = 'FAILED',
    updated_at = now()
WHERE id = $1;

-- name: MarkDone :exec
UPDATE video_jobs
SET status = 'DONE',
    updated_at = now()
WHERE id = $1;

-- name: CleanupStuckJobs :exec
UPDATE video_jobs
SET status = 'FAILED',
    updated_at = now()
WHERE status = 'PROCESSING'
  AND updated_at < now() - interval '30 minutes';

-- name: ResetStuckJobs :exec
UPDATE video_jobs
SET status = 'PENDING',
    updated_at = now()
WHERE status = 'PROCESSING'
  AND updated_at < now() - interval '10 minutes';

-- name: MarkRetry :exec
UPDATE video_jobs
SET retry_count = retry_count + 1,
    next_retry_at = now() + ($1::int * interval '1 second'),
    status = 'PENDING',
    updated_at = now()
WHERE id = $2;

-- name: LockAndMarkProcessing :many
WITH jobs AS (
    SELECT id
    FROM video_jobs
    WHERE status = 'PENDING'
    ORDER BY created_at
    LIMIT $1
    FOR UPDATE SKIP LOCKED
            )
UPDATE video_jobs
SET status = 'PROCESSING',
    updated_at = now()
WHERE id IN (SELECT id FROM jobs)
    RETURNING id, filename, retry_count;