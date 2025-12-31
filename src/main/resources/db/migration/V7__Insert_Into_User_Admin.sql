INSERT INTO users (
    user_name,
    password,
    full_name,
    email,
    enabled,
    account_non_expired,
    account_non_locked,
    credentials_non_expired
)
SELECT
    'admin',
    '{pbkdf2}cad31082bc186736c32b60bd3b35c0c75b0774020d826e75aebdb251f4f555496ed0a3fb7732a7a6',
    'Administrador do Sistema',
    'admin@system.com',
    true,
    true,
    true,
    true
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE user_name = 'admin'
);