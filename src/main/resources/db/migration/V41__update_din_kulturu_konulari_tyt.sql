-- Din Kültürü ve Ahlak Bilgisi dersinin konularını TYT konularıyla güncelle
-- 1. Mevcut konulara bağlı soruları "Genel" konusuna taşı
-- 2. Tüm Din Kültürü konularını sil (Genel hariç)
-- 3. Yeni TYT konularını ekle

DO $$
DECLARE
    din_kulturu_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    -- Din Kültürü ders ID'sini al
    SELECT id INTO din_kulturu_ders_id FROM ders WHERE ad = 'Din Kültürü' OR ad = 'Din Kültürü ve Ahlak Bilgisi';
    
    IF din_kulturu_ders_id IS NULL THEN
        RAISE EXCEPTION 'Din Kültürü dersi bulunamadı';
    END IF;
    
    -- "Genel" konusunu bul veya oluştur
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = din_kulturu_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (din_kulturu_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Din Kültürü ders ID: %', din_kulturu_ders_id;
    
    -- Mevcut konuları kontrol et ve soruları "Genel" konusuna taşı
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = din_kulturu_ders_id AND ad != 'Genel'
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
    
    -- Yeni TYT Din Kültürü ve Ahlak Bilgisi konularını ekle (sadece yoksa)
    INSERT INTO konu (ders_id, ad) VALUES
        (din_kulturu_ders_id, 'Bilgi ve İnanç'),
        (din_kulturu_ders_id, 'İslam ve İbadet'),
        (din_kulturu_ders_id, 'Ahlak ve Değerler'),
        (din_kulturu_ders_id, 'Allah İnsan İlişkisi'),
        (din_kulturu_ders_id, 'Hz. Muhammed (S.A.V.)'),
        (din_kulturu_ders_id, 'Vahiy ve Akıl'),
        (din_kulturu_ders_id, 'İslam Düşüncesinde Yorumlar, Mezhepler'),
        (din_kulturu_ders_id, 'Din, Kültür ve Medeniyet'),
        (din_kulturu_ders_id, 'İslam ve Bilim, Estetik, Barış'),
        (din_kulturu_ders_id, 'Yaşayan Dinler')
    ON CONFLICT (ders_id, ad) DO NOTHING;
    
    RAISE NOTICE '✅ TYT Din Kültürü ve Ahlak Bilgisi konuları eklendi/güncellendi (Toplam: 10 konu)';
END $$;
