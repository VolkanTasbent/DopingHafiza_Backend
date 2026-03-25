-- Kullanicinin AI ile olusturdugu calisma programlarini sunucuda saklar
CREATE TABLE IF NOT EXISTS ai_saved_study_plan (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    title           VARCHAR(500) NOT NULL,
    payload_json    TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_saved_plan_user_created
    ON ai_saved_study_plan(user_id, created_at DESC);
