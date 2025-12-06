-- User tablosuna dark_mode kolonu ekle
ALTER TABLE app_user 
ADD COLUMN IF NOT EXISTS dark_mode BOOLEAN DEFAULT false;

-- Index ekle (opsiyonel - arama performansı için)
CREATE INDEX IF NOT EXISTS idx_user_dark_mode ON app_user(dark_mode);

COMMENT ON COLUMN app_user.dark_mode IS 'Kullanıcının dark mode tercihi';

