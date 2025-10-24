-- Quiz oturumunu kullanıcıya bağla
ALTER TABLE quiz_oturumu
    ADD COLUMN IF NOT EXISTS user_id BIGINT;

ALTER TABLE quiz_oturumu
    ADD CONSTRAINT IF NOT EXISTS fk_quiz_user
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_quiz_user ON quiz_oturumu(user_id);
