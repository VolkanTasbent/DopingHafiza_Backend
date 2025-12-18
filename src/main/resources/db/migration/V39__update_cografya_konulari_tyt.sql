-- Coğrafya dersinin konularını TYT konularıyla güncelle
-- 1. Mevcut konulara bağlı soruları "Genel" konusuna taşı
-- 2. Tüm Coğrafya konularını sil (Genel hariç)
-- 3. Yeni TYT konularını ekle

DO $$
DECLARE
    cografya_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    -- Coğrafya ders ID'sini al
    SELECT id INTO cografya_ders_id FROM ders WHERE ad = 'Coğrafya';
    
    IF cografya_ders_id IS NULL THEN
        RAISE EXCEPTION 'Coğrafya dersi bulunamadı';
    END IF;
    
    -- "Genel" konusunu bul veya oluştur
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = cografya_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (cografya_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Coğrafya ders ID: %', cografya_ders_id;
    
    -- Mevcut konuları kontrol et ve soruları "Genel" konusuna taşı
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = cografya_ders_id AND ad != 'Genel'
    LOOP
        -- Bu konuya bağlı soru sayısını kontrol et
        SELECT COUNT(*) INTO soru_count 
        FROM soru_konu 
        WHERE konu_id = konu_rec.id;
        
        RAISE NOTICE 'Konu: "%" (ID: %) - Bağlı soru sayısı: %', konu_rec.ad, konu_rec.id, soru_count;
        
        -- Eğer soru varsa, soruları "Genel" konusuna taşı
        IF soru_count > 0 THEN
            -- Önce "Genel" konusuna bağlı olmayan soruları ekle
            INSERT INTO soru_konu (soru_id, konu_id)
            SELECT DISTINCT sk.soru_id, genel_konu_id
            FROM soru_konu sk
            WHERE sk.konu_id = konu_rec.id
            AND sk.soru_id NOT IN (
                SELECT soru_id FROM soru_konu WHERE konu_id = genel_konu_id
            )
            ON CONFLICT DO NOTHING;
            
            GET DIAGNOSTICS tasinan_soru_count = ROW_COUNT;
            RAISE NOTICE '  ✅ % soru "Genel" konusuna taşındı', tasinan_soru_count;
            
            -- Eski konuya olan tüm ilişkileri sil
            DELETE FROM soru_konu WHERE konu_id = konu_rec.id;
        END IF;
        
        -- Konuyu sil
        DELETE FROM konu WHERE id = konu_rec.id;
        RAISE NOTICE '  ✅ Konu silindi: "%"', konu_rec.ad;
    END LOOP;
    
    -- Yeni TYT Coğrafya konularını ekle (sadece yoksa)
    INSERT INTO konu (ders_id, ad) VALUES
        (cografya_ders_id, 'Doğa ve İnsan'),
        (cografya_ders_id, 'Dünya''nın Şekli ve Hareketleri'),
        (cografya_ders_id, 'Coğrafi Konum'),
        (cografya_ders_id, 'Harita Bilgisi'),
        (cografya_ders_id, 'Atmosfer ve Sıcaklık'),
        (cografya_ders_id, 'İklimler'),
        (cografya_ders_id, 'Basınç ve Rüzgarlar'),
        (cografya_ders_id, 'Nem, Yağış ve Buharlaşma'),
        (cografya_ders_id, 'İç Kuvvetler / Dış Kuvvetler'),
        (cografya_ders_id, 'Su – Toprak ve Bitkiler'),
        (cografya_ders_id, 'Nüfus'),
        (cografya_ders_id, 'Göç'),
        (cografya_ders_id, 'Yerleşme'),
        (cografya_ders_id, 'Türkiye''nin Yer Şekilleri'),
        (cografya_ders_id, 'Ekonomik Faaliyetler'),
        (cografya_ders_id, 'Bölgeler'),
        (cografya_ders_id, 'Uluslararası Ulaşım Hatları'),
        (cografya_ders_id, 'Çevre ve Toplum'),
        (cografya_ders_id, 'Doğal Afetler')
    ON CONFLICT (ders_id, ad) DO NOTHING;
    
    RAISE NOTICE '✅ TYT Coğrafya konuları eklendi/güncellendi (Toplam: 19 konu)';
END $$;

