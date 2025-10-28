-- Profil resmi desteği için avatar_url kolonu ekleme
ALTER TABLE app_user 
ADD COLUMN IF NOT EXISTS avatar_url TEXT;

COMMENT ON COLUMN app_user.avatar_url IS 'Kullanıcının profil resmi URL yolu';

