-- Video not alma tablosu
CREATE TABLE IF NOT EXISTS video_note (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    konu_id BIGINT NOT NULL REFERENCES konu(id) ON DELETE CASCADE,
    video_url TEXT NOT NULL,
    note_text TEXT NOT NULL,
    timestamp_seconds INTEGER NOT NULL, -- Video'daki zaman damgası (saniye)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_video_note_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_video_note_konu FOREIGN KEY (konu_id) REFERENCES konu(id) ON DELETE CASCADE
);

-- Index'ler
CREATE INDEX IF NOT EXISTS idx_video_note_user ON video_note(user_id);
CREATE INDEX IF NOT EXISTS idx_video_note_konu ON video_note(konu_id);
CREATE INDEX IF NOT EXISTS idx_video_note_user_konu ON video_note(user_id, konu_id);
CREATE INDEX IF NOT EXISTS idx_video_note_timestamp ON video_note(timestamp_seconds);

COMMENT ON TABLE video_note IS 'Video izlerken alınan notlar';
COMMENT ON COLUMN video_note.timestamp_seconds IS 'Video içindeki zaman damgası (saniye cinsinden)';
COMMENT ON COLUMN video_note.note_text IS 'Kullanıcının yazdığı not metni';








