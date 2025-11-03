-- Sosyal Bilimler dersini ve ilişkili tüm verileri sil

-- Önce soru_konu ilişkilerini sil
DELETE FROM soru_konu 
WHERE soru_id IN (
    SELECT id FROM soru 
    WHERE ders_id IN (SELECT id FROM ders WHERE ad = 'Sosyal Bilimler')
);

-- Cevap kayıtlarını sil
DELETE FROM cevap 
WHERE soru_id IN (
    SELECT id FROM soru 
    WHERE ders_id IN (SELECT id FROM ders WHERE ad = 'Sosyal Bilimler')
);

-- Seçenekleri sil
DELETE FROM secenek 
WHERE soru_id IN (
    SELECT id FROM soru 
    WHERE ders_id IN (SELECT id FROM ders WHERE ad = 'Sosyal Bilimler')
);

-- Soruları sil
DELETE FROM soru 
WHERE ders_id IN (SELECT id FROM ders WHERE ad = 'Sosyal Bilimler');

-- Konuları sil (sadece Sosyal Bilimler dersine ait olanlar)
DELETE FROM konu 
WHERE ders_id IN (SELECT id FROM ders WHERE ad = 'Sosyal Bilimler');

-- Dersi sil
DELETE FROM ders WHERE ad = 'Sosyal Bilimler';

