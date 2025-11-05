-- Deneme sınavları tablosu
CREATE TABLE IF NOT EXISTS deneme_sinavi (
    id BIGSERIAL PRIMARY KEY,
    ad VARCHAR(200) NOT NULL,
    tip VARCHAR(10) NOT NULL CHECK (tip IN ('TYT', 'AYT')),
    olusturma_tarihi TIMESTAMPTZ DEFAULT NOW(),
    aciklama TEXT
);

COMMENT ON TABLE deneme_sinavi IS 'Deneme sınavları (TYT/AYT)';
COMMENT ON COLUMN deneme_sinavi.tip IS 'Sınav tipi: TYT veya AYT';

-- Deneme sınavı soruları tablosu
CREATE TABLE IF NOT EXISTS deneme_sinavi_soru (
    id BIGSERIAL PRIMARY KEY,
    deneme_sinavi_id BIGINT NOT NULL REFERENCES deneme_sinavi(id) ON DELETE CASCADE,
    soru_no INTEGER NOT NULL,
    soru_metni TEXT NOT NULL,
    sik_a TEXT,
    sik_b TEXT,
    sik_c TEXT,
    sik_d TEXT,
    sik_e TEXT,
    dogru_cevap CHAR(1) NOT NULL CHECK (dogru_cevap IN ('A', 'B', 'C', 'D', 'E')),
    zorluk INTEGER CHECK (zorluk >= 1 AND zorluk <= 5),
    konular TEXT, -- Virgülle ayrılmış konu adları
    aciklama TEXT,
    olusturma_tarihi TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(deneme_sinavi_id, soru_no)
);

COMMENT ON TABLE deneme_sinavi_soru IS 'Deneme sınavı soruları (normal soru tablosundan ayrı)';
COMMENT ON COLUMN deneme_sinavi_soru.konular IS 'Virgülle ayrılmış konu adları (örn: "Fonksiyonlar,Logaritma")';

-- Index'ler
CREATE INDEX IF NOT EXISTS idx_deneme_sinavi_tip ON deneme_sinavi(tip);
CREATE INDEX IF NOT EXISTS idx_deneme_sinavi_soru_deneme_id ON deneme_sinavi_soru(deneme_sinavi_id);
CREATE INDEX IF NOT EXISTS idx_deneme_sinavi_soru_no ON deneme_sinavi_soru(deneme_sinavi_id, soru_no);


