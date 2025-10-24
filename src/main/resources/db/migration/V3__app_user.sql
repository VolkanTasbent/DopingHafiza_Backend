-- Kimlik tablosu (Auth/JWT için)
CREATE TABLE IF NOT EXISTS app_user (
                                        id BIGSERIAL PRIMARY KEY,
                                        email  VARCHAR(150) UNIQUE NOT NULL,
    ad     VARCHAR(60)  NOT NULL,
    soyad  VARCHAR(60)  NOT NULL,
    password VARCHAR(200) NOT NULL,   -- BCrypt hash
    role   VARCHAR(30) NOT NULL DEFAULT 'USER',  -- USER / ADMIN
    enabled BOOLEAN NOT NULL DEFAULT TRUE
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_email ON app_user(email);
