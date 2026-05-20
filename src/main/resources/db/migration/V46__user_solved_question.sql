CREATE TABLE IF NOT EXISTS user_solved_question (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    soru_id    BIGINT NOT NULL REFERENCES soru(id) ON DELETE CASCADE,
    cozuldu    BOOLEAN NOT NULL DEFAULT TRUE,
    cozuldu_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dogru      BOOLEAN,
    oturum_id  BIGINT REFERENCES quiz_oturumu(id) ON DELETE SET NULL,
    CONSTRAINT uk_user_solved_question UNIQUE (user_id, soru_id)
);

CREATE INDEX IF NOT EXISTS idx_user_solved_user ON user_solved_question(user_id);
CREATE INDEX IF NOT EXISTS idx_user_solved_soru ON user_solved_question(soru_id);

INSERT INTO user_solved_question (user_id, soru_id, cozuldu, cozuldu_at, dogru, oturum_id)
SELECT DISTINCT qo.user_id, c.soru_id, TRUE, COALESCE(qo.finished_at, qo.started_at, NOW()), c.dogru, qo.id
FROM cevap c
JOIN quiz_oturumu qo ON qo.id = c.oturum_id
WHERE qo.user_id IS NOT NULL
  AND c.soru_id IS NOT NULL
ON CONFLICT (user_id, soru_id) DO NOTHING;
