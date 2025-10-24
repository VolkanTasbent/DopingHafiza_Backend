-- Örnek ders/soru/seçenekler
INSERT INTO ders (ad) VALUES ('Matematik')
    ON CONFLICT (ad) DO NOTHING;

-- 2 + 2 sorusu
WITH d AS (SELECT id FROM ders WHERE ad='Matematik')
INSERT INTO soru (ders_id, metin, tip, zorluk)
SELECT d.id, '2 + 2 kaçtır?', 'coktan_secmeli', 1 FROM d
    ON CONFLICT DO NOTHING;

WITH s AS (SELECT id FROM soru WHERE metin='2 + 2 kaçtır?')
INSERT INTO secenek (soru_id, metin, dogru, siralama)
SELECT s.id, '3', false, 1 FROM s
    ON CONFLICT DO NOTHING;

WITH s AS (SELECT id FROM soru WHERE metin='2 + 2 kaçtır?')
INSERT INTO secenek (soru_id, metin, dogru, siralama)
SELECT s.id, '4', true,  2 FROM s
    ON CONFLICT DO NOTHING;
