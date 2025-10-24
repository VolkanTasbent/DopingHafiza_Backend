CREATE TABLE IF NOT EXISTS tag (
                                   id  bigserial PRIMARY KEY,
                                   ad  varchar(120) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS soru_tag (
                                        soru_id bigint NOT NULL REFERENCES soru(id) ON DELETE CASCADE,
    tag_id  bigint NOT NULL REFERENCES tag(id)  ON DELETE CASCADE,
    PRIMARY KEY (soru_id, tag_id)
    );

CREATE INDEX IF NOT EXISTS idx_soru_tag_soru ON soru_tag(soru_id);
CREATE INDEX IF NOT EXISTS idx_soru_tag_tag  ON soru_tag(tag_id);
