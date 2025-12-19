-- Konu video tablosu (bir konuya birden fazla video eklenebilir)
CREATE TABLE IF NOT EXISTS konu_video (
    id BIGSERIAL PRIMARY KEY,
    konu_id BIGINT NOT NULL REFERENCES konu(id) ON DELETE CASCADE,
    video_url VARCHAR(500) NOT NULL,
    video_adi VARCHAR(255),
    siralama INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index'ler
CREATE INDEX IF NOT EXISTS idx_konu_video_konu ON konu_video(konu_id);
CREATE INDEX IF NOT EXISTS idx_konu_video_siralama ON konu_video(konu_id, siralama);

COMMENT ON TABLE konu_video IS 'Konu anlatım videoları (bir konuya birden fazla video eklenebilir)';
COMMENT ON COLUMN konu_video.video_url IS 'Video URL (dosya yolu veya YouTube/Vimeo linki)';
COMMENT ON COLUMN konu_video.video_adi IS 'Video adı (opsiyonel)';
COMMENT ON COLUMN konu_video.siralama IS 'Video sıralaması (küçükten büyüğe)';



