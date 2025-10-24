CREATE TABLE IF NOT EXISTS tavsiye (
                                       id               bigserial PRIMARY KEY,
                                       kullanici_id     bigint REFERENCES app_user(id) ON DELETE CASCADE,
    icerik           text NOT NULL,
    olusturma_tarihi timestamptz NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_tavsiye_kullanici ON tavsiye(kullanici_id);
