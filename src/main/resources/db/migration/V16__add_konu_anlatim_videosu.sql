-- Konu anlatım videosu URL kolonu ekle
ALTER TABLE konu 
ADD COLUMN IF NOT EXISTS konu_anlatim_videosu_url VARCHAR(500);

COMMENT ON COLUMN konu.konu_anlatim_videosu_url IS 'Konu anlatım videosu URL yolu (video dosyası veya YouTube/Vimeo linki)';

