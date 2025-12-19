-- Hedef sıralama kolonu ekle
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS hedef_siralama INTEGER;

-- Varsayılan değer (opsiyonel)
UPDATE app_user SET hedef_siralama = 10000 WHERE hedef_siralama IS NULL;

COMMENT ON COLUMN app_user.hedef_siralama IS 'Kullanıcının hedef sıralaması';










