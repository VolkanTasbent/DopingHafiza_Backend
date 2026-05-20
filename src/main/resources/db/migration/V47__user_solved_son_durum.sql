-- BOS: tekrar listede gelir; DOGRU/YANLIS: atlanir
ALTER TABLE user_solved_question
    ADD COLUMN IF NOT EXISTS son_durum VARCHAR(10);

UPDATE user_solved_question usq
SET son_durum = CASE
    WHEN usq.dogru IS NULL THEN 'BOS'
    WHEN usq.dogru = TRUE THEN 'DOGRU'
    ELSE 'YANLIS'
END
WHERE son_durum IS NULL;

-- Gecmis cevaplardan son durumu yukle (en son oturum)
UPDATE user_solved_question usq
SET son_durum = latest.durum
FROM (
    SELECT DISTINCT ON (qo.user_id, c.soru_id)
        qo.user_id,
        c.soru_id,
        CASE
            WHEN c.secenek_id IS NULL THEN 'BOS'
            WHEN c.dogru = TRUE THEN 'DOGRU'
            ELSE 'YANLIS'
        END AS durum
    FROM cevap c
    JOIN quiz_oturumu qo ON qo.id = c.oturum_id
    WHERE qo.user_id IS NOT NULL
    ORDER BY qo.user_id, c.soru_id, COALESCE(qo.finished_at, qo.started_at) DESC
) latest
WHERE usq.user_id = latest.user_id
  AND usq.soru_id = latest.soru_id;

UPDATE user_solved_question SET son_durum = 'YANLIS' WHERE son_durum IS NULL;

ALTER TABLE user_solved_question
    ALTER COLUMN son_durum SET NOT NULL;

ALTER TABLE user_solved_question
    ALTER COLUMN son_durum SET DEFAULT 'BOS';
