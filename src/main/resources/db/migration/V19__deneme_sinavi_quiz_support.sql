-- Quiz oturumuna deneme sınavı desteği ekle
ALTER TABLE quiz_oturumu 
ADD COLUMN IF NOT EXISTS deneme_sinavi_id BIGINT REFERENCES deneme_sinavi(id) ON DELETE SET NULL;

COMMENT ON COLUMN quiz_oturumu.deneme_sinavi_id IS 'Eğer bu oturum bir deneme sınavı ise, hangi deneme sınavı olduğunu gösterir. NULL ise normal soru çözme oturumudur.';

CREATE INDEX IF NOT EXISTS idx_quiz_oturumu_deneme_sinavi ON quiz_oturumu(deneme_sinavi_id);


