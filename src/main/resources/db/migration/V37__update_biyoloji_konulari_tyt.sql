-- Biyoloji dersinin konularını TYT konularıyla güncelle
-- 1. Mevcut konulara bağlı soruları "Genel" konusuna taşı
-- 2. Tüm Biyoloji konularını sil (Genel hariç)
-- 3. Yeni TYT konularını ekle

DO $$
DECLARE
    biyoloji_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    -- Biyoloji ders ID'sini al
    SELECT id INTO biyoloji_ders_id FROM ders WHERE ad = 'Biyoloji';
    
    IF biyoloji_ders_id IS NULL THEN
        RAISE EXCEPTION 'Biyoloji dersi bulunamadı';
    END IF;
    
    -- "Genel" konusunu bul veya oluştur
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = biyoloji_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (biyoloji_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Biyoloji ders ID: %', biyoloji_ders_id;
    
    -- Mevcut konuları kontrol et ve soruları "Genel" konusuna taşı
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = biyoloji_ders_id AND ad != 'Genel'
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
    
    -- Yeni TYT Biyoloji konularını ekle (sadece yoksa)
    INSERT INTO konu (ders_id, ad) VALUES
        (biyoloji_ders_id, 'Canlıların Ortak Özellikleri'),
        (biyoloji_ders_id, 'Canlıların Temel Bileşenleri'),
        (biyoloji_ders_id, 'Hücre ve Organelleri'),
        (biyoloji_ders_id, 'Hücre Zarından Madde Geçişi'),
        (biyoloji_ders_id, 'Canlıların Sınıflandırılması'),
        (biyoloji_ders_id, 'Mitoz ve Eşeysiz Üreme'),
        (biyoloji_ders_id, 'Mayoz ve Eşeyli Üreme'),
        (biyoloji_ders_id, 'Kalıtım'),
        (biyoloji_ders_id, 'Ekosistem Ekolojisi'),
        (biyoloji_ders_id, 'Güncel Çevre Sorunları')
    ON CONFLICT (ders_id, ad) DO NOTHING;
    
    RAISE NOTICE '✅ TYT Biyoloji konuları eklendi/güncellendi (Toplam: 10 konu)';
END $$;

