-- Any user who owns a job but lacks the CLIENT role gets it.
-- Preserves their ability to update/delete their existing postings
-- and makes their account consistent with the new "only CLIENTs can post" rule.
-- Idempotent: safe to run on an empty jobs table or on a DB where every job
-- owner already has the CLIENT role.
INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT j.client_id, r.id
FROM jobs j
CROSS JOIN roles r
WHERE r.name = 'CLIENT'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = j.client_id
        AND ur.role_id = r.id
  );