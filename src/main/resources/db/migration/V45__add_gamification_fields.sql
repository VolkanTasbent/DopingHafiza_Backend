-- Gamification alanlari: altin bakiyesi + kalici oyun durumu JSON
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS altin INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS gamification_state TEXT;

UPDATE app_user
SET altin = 0
WHERE altin IS NULL;

COMMENT ON COLUMN app_user.altin IS 'Kullanicinin altin bakiyesi';
COMMENT ON COLUMN app_user.gamification_state IS 'Gorevler/market/efektler dahil kalici oyun durumu JSON';

CREATE INDEX IF NOT EXISTS idx_user_altin ON app_user(altin DESC);
