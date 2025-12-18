-- Kullanıcı puan kolonu ekle
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS puan INTEGER DEFAULT 0;

-- Index ekle (sıralama için)
CREATE INDEX IF NOT EXISTS idx_user_puan ON app_user(puan DESC);

-- Mevcut kullanıcıların puanını 0 yap
UPDATE app_user SET puan = 0 WHERE puan IS NULL;

COMMENT ON COLUMN app_user.puan IS 'Kullanıcının performans puanı (dinamik hesaplanır)';




