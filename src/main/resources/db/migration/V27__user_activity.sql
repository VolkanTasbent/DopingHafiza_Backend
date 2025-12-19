-- Son Aktivitelerim tablosu
CREATE TABLE IF NOT EXISTS user_activity (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL, -- 'soru_cozme', 'video_izleme', 'konu_calisma', 'ders_tamamlama', 'pomodoro'
    activity_title VARCHAR(255) NOT NULL, -- Örn: "TYT Tarih > Tarih ve Zaman"
    activity_subtitle VARCHAR(255), -- Örn: "Tarihin Tanımı, Yöntemi ve..."
    activity_icon VARCHAR(50) DEFAULT 'document', -- 'document', 'video', 'book', 'grid', 'abc'
    ders_id BIGINT REFERENCES ders(id) ON DELETE SET NULL,
    konu_id BIGINT REFERENCES konu(id) ON DELETE SET NULL,
    rapor_id BIGINT REFERENCES quiz_oturumu(id) ON DELETE SET NULL, -- Soru çözme aktiviteleri için
    metadata JSONB, -- Ek bilgiler (soru sayısı, süre, net vb.)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_activity_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Index'ler
CREATE INDEX IF NOT EXISTS idx_user_activity_user_date ON user_activity(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_activity_type ON user_activity(activity_type);
CREATE INDEX IF NOT EXISTS idx_user_activity_created_at ON user_activity(created_at DESC);

COMMENT ON TABLE user_activity IS 'Kullanıcı aktivite kayıtları (soru çözme, video izleme, konu çalışma vb.)';
COMMENT ON COLUMN user_activity.activity_type IS 'Aktivite tipi: soru_cozme, video_izleme, konu_calisma, ders_tamamlama, pomodoro';
COMMENT ON COLUMN user_activity.activity_title IS 'Aktivite başlığı (örn: "TYT Tarih > Tarih ve Zaman")';
COMMENT ON COLUMN user_activity.activity_subtitle IS 'Aktivite alt başlığı (detay bilgisi)';
COMMENT ON COLUMN user_activity.activity_icon IS 'Aktivite ikonu: document, video, book, grid, abc';
COMMENT ON COLUMN user_activity.metadata IS 'Ek bilgiler (JSON formatında: soru sayısı, süre, net vb.)';








