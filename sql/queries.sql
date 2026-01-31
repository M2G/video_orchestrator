-- name: UpsertVideoJob :exec
INSERT INTO video_jobs (filename, status)
VALUES ($1, $2)
    ON CONFLICT (filename)
DO UPDATE SET
    status = EXCLUDED.status,
           updated_at = now();

-- name: IncrementRetry :exec
UPDATE video_jobs
SET retry_count = retry_count + 1,
    updated_at = now()
WHERE filename = $1;

-- name: FindRunnableJobs :many
SELECT filename, retry_count
FROM video_jobs
WHERE status = 'PENDING'
  AND retry_count < 5;

-- name: LockJob :exec
UPDATE video_jobs
SET status = 'PROCESSING',
    updated_at = now()
WHERE filename = $1
  AND status = 'PENDING';
