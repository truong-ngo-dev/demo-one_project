-- V7: Migrate flat user_roles → user_role_context (ADMIN scope)
-- Removes the user_roles join table; all role assignments now live in user_role_context.

-- Step 1: Create ADMIN context rows for every user that has at least one role in user_roles
INSERT IGNORE INTO user_role_context (user_id, scope, org_id)
SELECT DISTINCT user_id, 'ADMIN', ''
FROM user_roles;

-- Step 2: Copy role assignments into user_role_context_roles
INSERT IGNORE INTO user_role_context_roles (context_id, role_id)
SELECT urc.id, ur.role_id
FROM user_roles ur
         JOIN user_role_context urc
              ON urc.user_id = ur.user_id
                  AND urc.scope = 'ADMIN'
                  AND urc.org_id = '';

-- Step 3: Drop the old flat join table
DROP TABLE user_roles;
