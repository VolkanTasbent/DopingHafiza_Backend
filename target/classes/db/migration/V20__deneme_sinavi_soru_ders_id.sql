-- Deneme sınavı sorularına ders_id ekle (her soru hangi derse ait)
ALTER TABLE deneme_sinavi_soru 
ADD COLUMN IF NOT EXISTS ders_id BIGINT REFERENCES ders(id) ON DELETE SET NULL;

COMMENT ON COLUMN deneme_sinavi_soru.ders_id IS 'Sorunun ait olduğu ders (Matematik, Fizik, vb.)';

CREATE INDEX IF NOT EXISTS idx_deneme_sinavi_soru_ders ON deneme_sinavi_soru(ders_id);

