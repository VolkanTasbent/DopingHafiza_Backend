-- Boş bırakılan soru sayısı için kolon ekleme
ALTER TABLE quiz_oturumu 
ADD COLUMN IF NOT EXISTS empty INTEGER DEFAULT 0;

COMMENT ON COLUMN quiz_oturumu.empty IS 'Boş bırakılan soru sayısı';

-- Eski kayıtları güncelle (total - correct - wrong = empty)
UPDATE quiz_oturumu 
SET empty = COALESCE(total, 0) - COALESCE(correct, 0) - COALESCE(wrong, 0)
WHERE empty IS NULL OR empty = 0;

