-- Konu tablosuna aciklama kolonu ekle
ALTER TABLE konu 
ADD COLUMN IF NOT EXISTS aciklama TEXT;

COMMENT ON COLUMN konu.aciklama IS 'Konu açıklaması (opsiyonel)';





