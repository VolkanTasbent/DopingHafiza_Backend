-- Kullanıcı streak (ardışık günlük aktivite) tablosu
CREATE TABLE IF NOT EXISTS user_streak (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    current_streak INTEGER DEFAULT 0,
    longest_streak INTEGER DEFAULT 0,
    last_activity_date DATE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_streak_user FOREIGN KEY (user_id) 
        REFERENCES app_user(id) ON DELETE CASCADE
);

-- Index'ler
CREATE INDEX IF NOT EXISTS idx_user_streak_user_id ON user_streak(user_id);
CREATE INDEX IF NOT EXISTS idx_user_streak_last_activity ON user_streak(last_activity_date);

-- Mevcut kullanıcılar için streak kayıtları oluştur
INSERT INTO user_streak (user_id, current_streak, longest_streak, last_activity_date)
SELECT id, 0, 0, NULL
FROM app_user
WHERE id NOT IN (SELECT user_id FROM user_streak)
ON CONFLICT (user_id) DO NOTHING;





