-- Konulara PDF döküman ekleme
ALTER TABLE konu 
ADD COLUMN IF NOT EXISTS dokuman_url TEXT,
ADD COLUMN IF NOT EXISTS dokuman_adi TEXT;

COMMENT ON COLUMN konu.dokuman_url IS 'Konu dökümanı (PDF) URL yolu';
COMMENT ON COLUMN konu.dokuman_adi IS 'Döküman dosya adı';





