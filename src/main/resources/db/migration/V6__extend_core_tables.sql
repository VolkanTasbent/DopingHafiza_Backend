-- DERS
ALTER TABLE ders
    ADD COLUMN IF NOT EXISTS aciklama        text,
    ADD COLUMN IF NOT EXISTS olusturma_tarihi timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS hedef_yks       text,
    ADD COLUMN IF NOT EXISTS sinif_alani     text,
    ADD COLUMN IF NOT EXISTS aktif           boolean NOT NULL DEFAULT true;

-- SORU
ALTER TABLE soru
    ADD COLUMN IF NOT EXISTS aciklama        text,
    ADD COLUMN IF NOT EXISTS zorluk          int,
    ADD COLUMN IF NOT EXISTS soru_no         int,
    ADD COLUMN IF NOT EXISTS image_url       text,
    ADD COLUMN IF NOT EXISTS olusturma_tarihi timestamptz NOT NULL DEFAULT now();

CREATE INDEX IF NOT EXISTS idx_soru_ders ON soru(ders_id);

-- QUIZ_OTURUMU
ALTER TABLE quiz_oturumu
    ADD COLUMN IF NOT EXISTS baslangic     timestamptz,
    ADD COLUMN IF NOT EXISTS bitis         timestamptz,
    ADD COLUMN IF NOT EXISTS duration_ms   bigint,
    ADD COLUMN IF NOT EXISTS total         int,
    ADD COLUMN IF NOT EXISTS correct       int,
    ADD COLUMN IF NOT EXISTS wrong         int,
    ADD COLUMN IF NOT EXISTS score         int,
    ADD COLUMN IF NOT EXISTS created_at    timestamptz NOT NULL DEFAULT now();

-- CEVAP
ALTER TABLE cevap
    ADD COLUMN IF NOT EXISTS kullanici_id  bigint REFERENCES app_user(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS acik_cevap    text,
    ADD COLUMN IF NOT EXISTS tarih         timestamptz NOT NULL DEFAULT now();

CREATE INDEX IF NOT EXISTS idx_cevap_oturum   ON cevap(quiz_oturumu_id);
CREATE INDEX IF NOT EXISTS idx_cevap_kullanici ON cevap(kullanici_id);

-- (Opsiyonel) app_user zaman damgaları
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS created_at timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now();
