-- Pomodoro Timer oturumları tablosu
CREATE TABLE IF NOT EXISTS pomodoro_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    duration INTEGER NOT NULL, -- Dakika cinsinden (örn: 25)
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_pomodoro_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Index'ler
CREATE INDEX IF NOT EXISTS idx_pomodoro_user_date ON pomodoro_session(user_id, completed_at);
CREATE INDEX IF NOT EXISTS idx_pomodoro_completed_at ON pomodoro_session(completed_at);

COMMENT ON TABLE pomodoro_session IS 'Pomodoro Timer oturum kayıtları';
COMMENT ON COLUMN pomodoro_session.duration IS 'Pomodoro süresi (dakika cinsinden)';
COMMENT ON COLUMN pomodoro_session.completed_at IS 'Pomodoro oturumunun tamamlandığı tarih/saat';









