-- Hedef üniversite, bölüm ve puan kolonları ekle
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS hedef_universite VARCHAR(255),
    ADD COLUMN IF NOT EXISTS hedef_bolum VARCHAR(255),
    ADD COLUMN IF NOT EXISTS hedef_puan DECIMAL(10, 2);

-- Index ekle (opsiyonel - arama performansı için)
CREATE INDEX IF NOT EXISTS idx_user_hedef ON app_user(hedef_universite, hedef_bolum);

COMMENT ON COLUMN app_user.hedef_universite IS 'Kullanıcının hedef üniversitesi';
COMMENT ON COLUMN app_user.hedef_bolum IS 'Kullanıcının hedef bölümü';
COMMENT ON COLUMN app_user.hedef_puan IS 'Kullanıcının hedef puanı';

