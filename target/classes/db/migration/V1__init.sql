-- Temel tablo ve ilişkiler

CREATE TABLE IF NOT EXISTS ders (
                                    id BIGSERIAL PRIMARY KEY,
                                    ad VARCHAR(120) UNIQUE NOT NULL
    );

CREATE TABLE IF NOT EXISTS soru (
                                    id BIGSERIAL PRIMARY KEY,
                                    ders_id BIGINT NOT NULL REFERENCES ders(id) ON DELETE CASCADE,
    metin VARCHAR(1000) NOT NULL,
    tip VARCHAR(40),
    zorluk INT,
    image_url VARCHAR(500)
    );
CREATE INDEX IF NOT EXISTS idx_soru_ders ON soru(ders_id);

CREATE TABLE IF NOT EXISTS secenek (
                                       id BIGSERIAL PRIMARY KEY,
                                       soru_id BIGINT NOT NULL REFERENCES soru(id) ON DELETE CASCADE,
    metin VARCHAR(500) NOT NULL,
    dogru BOOLEAN NOT NULL,
    siralama INT
    );
CREATE INDEX IF NOT EXISTS idx_secenek_soru ON secenek(soru_id);

CREATE TABLE IF NOT EXISTS quiz_oturumu (
                                            id BIGSERIAL PRIMARY KEY,
                                            started_at TIMESTAMP,
                                            finished_at TIMESTAMP,
                                            duration_ms BIGINT,
                                            total INT,
                                            correct INT,
                                            wrong INT,
                                            score INT
);

CREATE TABLE IF NOT EXISTS cevap (
                                     id BIGSERIAL PRIMARY KEY,
                                     oturum_id BIGINT NOT NULL REFERENCES quiz_oturumu(id) ON DELETE CASCADE,
    soru_id   BIGINT NOT NULL REFERENCES soru(id) ON DELETE CASCADE,
    secenek_id BIGINT REFERENCES secenek(id) ON DELETE SET NULL,
    dogru BOOLEAN NOT NULL
    );
CREATE INDEX IF NOT EXISTS idx_cevap_oturum ON cevap(oturum_id);
CREATE INDEX IF NOT EXISTS idx_cevap_soru   ON cevap(soru_id);
