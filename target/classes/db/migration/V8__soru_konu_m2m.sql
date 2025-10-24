-- Çoktan-çoğa tablo
CREATE TABLE IF NOT EXISTS soru_konu (
                                         soru_id bigint NOT NULL REFERENCES soru(id) ON DELETE CASCADE,
    konu_id bigint NOT NULL REFERENCES konu(id) ON DELETE RESTRICT,
    PRIMARY KEY (soru_id, konu_id)
    );

-- Eğer soru tablosunda HALEN konu_id sütunu varsa, değerleri taşı
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='soru' AND column_name='konu_id'
  ) THEN
    INSERT INTO soru_konu (soru_id, konu_id)
SELECT id AS soru_id, konu_id
FROM soru
WHERE konu_id IS NOT NULL
    ON CONFLICT DO NOTHING;

ALTER TABLE soru DROP CONSTRAINT IF EXISTS fk_soru_konu;
ALTER TABLE soru DROP COLUMN IF EXISTS konu_id;
END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_soru_konu_soru ON soru_konu(soru_id);
CREATE INDEX IF NOT EXISTS idx_soru_konu_konu ON soru_konu(konu_id);
