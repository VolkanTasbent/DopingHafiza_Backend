-- Deneme sınavı soruları için çözüm videosu desteği
ALTER TABLE deneme_sinavi_soru 
ADD COLUMN IF NOT EXISTS cozum_videosu_url VARCHAR(500);

COMMENT ON COLUMN deneme_sinavi_soru.cozum_videosu_url IS 'Deneme sınavı sorusu çözüm videosu URL yolu (YouTube, Vimeo vb.)';

