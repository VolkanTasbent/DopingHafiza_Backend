-- Tarih dersinin konularını TYT konularıyla güncelle
-- 1. Mevcut konulara bağlı soruları "Genel" konusuna taşı
-- 2. Tüm Tarih konularını sil (Genel hariç)
-- 3. Yeni TYT konularını ekle

DO $$
DECLARE
    tarih_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    -- Tarih ders ID'sini al
    SELECT id INTO tarih_ders_id FROM ders WHERE ad = 'Tarih';
    
    IF tarih_ders_id IS NULL THEN
        RAISE EXCEPTION 'Tarih dersi bulunamadı';
    END IF;
    
    -- "Genel" konusunu bul veya oluştur
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = tarih_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (tarih_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Tarih ders ID: %', tarih_ders_id;
    
    -- Mevcut konuları kontrol et ve soruları "Genel" konusuna taşı
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = tarih_ders_id AND ad != 'Genel'
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
    
    -- Yeni TYT Tarih konularını ekle (sadece yoksa)
    INSERT INTO konu (ders_id, ad) VALUES
        (tarih_ders_id, 'Tarih ve Zaman'),
        (tarih_ders_id, 'İnsanlığın İlk Dönemleri'),
        (tarih_ders_id, 'Orta Çağ''da Dünya'),
        (tarih_ders_id, 'İlk ve Orta Çağlarda Türk Dünyası'),
        (tarih_ders_id, 'İslam Medeniyetinin Doğuşu'),
        (tarih_ders_id, 'Türklerin İslamiyet''i Kabulü ve İlk Türk İslam Devletleri'),
        (tarih_ders_id, 'Yerleşme ve Devletleşme Sürecinde Selçuklu Türkiyesi'),
        (tarih_ders_id, 'Beylikten Devlete Osmanlı Siyaseti'),
        (tarih_ders_id, 'Devletleşme Sürecinde Savaşçılar ve Askerler'),
        (tarih_ders_id, 'Beylikten Devlete Osmanlı Medeniyeti'),
        (tarih_ders_id, 'Dünya Gücü Osmanlı'),
        (tarih_ders_id, 'Sultan ve Osmanlı Merkez Teşkilatı'),
        (tarih_ders_id, 'Klasik Çağda Osmanlı Toplum Düzeni'),
        (tarih_ders_id, 'Değişen Dünya Dengeleri Karşısında Osmanlı Siyaseti'),
        (tarih_ders_id, 'Değişim Çağında Avrupa ve Osmanlı'),
        (tarih_ders_id, 'Uluslararası İlişkilerde Denge Stratejisi (1774-1914)'),
        (tarih_ders_id, 'Devrimler Çağında Değişen Devlet-Toplum İlişkileri'),
        (tarih_ders_id, 'Sermaye ve Emek'),
        (tarih_ders_id, 'XIX. ve XX. Yüzyılda Değişen Gündelik Hayat'),
        (tarih_ders_id, 'XX. Yüzyıl Başlarında Osmanlı Devleti ve Dünya'),
        (tarih_ders_id, 'Milli Mücadele'),
        (tarih_ders_id, 'Atatürkçülük ve Türk İnkılabı')
    ON CONFLICT (ders_id, ad) DO NOTHING;
    
    RAISE NOTICE '✅ TYT Tarih konuları eklendi/güncellendi (Toplam: 22 konu)';
END $$;

