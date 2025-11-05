-- Deneme sınavı cevapları için tablo
CREATE TABLE IF NOT EXISTS deneme_sinavi_cevap (
    id BIGSERIAL PRIMARY KEY,
    oturum_id BIGINT NOT NULL REFERENCES quiz_oturumu(id) ON DELETE CASCADE,
    deneme_sinavi_soru_id BIGINT NOT NULL REFERENCES deneme_sinavi_soru(id) ON DELETE CASCADE,
    soru_no INTEGER NOT NULL,
    secilen_cevap VARCHAR(1), -- "A", "B", "C", "D", "E" veya NULL (boş)
    dogru BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(oturum_id, soru_no)
);

CREATE INDEX IF NOT EXISTS idx_deneme_cevap_oturum ON deneme_sinavi_cevap(oturum_id);
CREATE INDEX IF NOT EXISTS idx_deneme_cevap_soru ON deneme_sinavi_cevap(deneme_sinavi_soru_id);

