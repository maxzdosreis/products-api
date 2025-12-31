-- Relaciona admin à ROLE_ADMIN
INSERT INTO user_permission (id_user, id_permission)
SELECT u.id, p.id
FROM users u
         JOIN permission p ON p.description = 'ROLE_ADMIN'
WHERE u.user_name = 'admin'
  AND NOT EXISTS (
    SELECT 1
    FROM user_permission up
    WHERE up.id_user = u.id
      AND up.id_permission = p.id
);