-- Deneme sınavı sorularına image_url kolonu ekle
ALTER TABLE deneme_sinavi_soru
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

COMMENT ON COLUMN deneme_sinavi_soru.image_url IS 'Soru görseli URL (opsiyonel)';

