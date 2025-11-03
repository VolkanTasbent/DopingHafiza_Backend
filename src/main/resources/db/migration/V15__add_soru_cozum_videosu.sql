-- Soru çözüm videosu desteği
ALTER TABLE soru 
ADD COLUMN IF NOT EXISTS cozum_videosu_url VARCHAR(500);

COMMENT ON COLUMN soru.cozum_videosu_url IS 'Soru çözüm videosu URL yolu (YouTube, Vimeo vb.)';


