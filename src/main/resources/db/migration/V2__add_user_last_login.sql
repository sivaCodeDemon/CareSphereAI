-- CareSphere AI — track the last successful login timestamp for audit/security purposes.
ALTER TABLE app_user ADD COLUMN last_login_at TIMESTAMPTZ;
