-- Türkçe dersinin konularını TYT konularıyla güncelle
-- 1. Mevcut konulara bağlı soruları "Genel" konusuna taşı
-- 2. Tüm Türkçe konularını sil (Genel hariç)
-- 3. Yeni TYT konularını ekle

DO $$
DECLARE
    turkce_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    -- Türkçe ders ID'sini al
    SELECT id INTO turkce_ders_id FROM ders WHERE ad = 'Türkçe';
    
    IF turkce_ders_id IS NULL THEN
        RAISE EXCEPTION 'Türkçe dersi bulunamadı';
    END IF;
    
    -- "Genel" konusunu bul veya oluştur
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = turkce_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (turkce_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Türkçe ders ID: %', turkce_ders_id;
    
    -- Mevcut konuları kontrol et ve soruları "Genel" konusuna taşı
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = turkce_ders_id AND ad != 'Genel'
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
    
    -- Yeni TYT Türkçe konularını ekle (sadece yoksa)
    INSERT INTO konu (ders_id, ad) VALUES
        (turkce_ders_id, 'Sözcükte Anlam'),
        (turkce_ders_id, 'Söz Yorumu'),
        (turkce_ders_id, 'Deyim ve Atasözü'),
        (turkce_ders_id, 'Cümlede Anlam'),
        (turkce_ders_id, 'Paragraf'),
        (turkce_ders_id, 'Paragrafta Anlatım Teknikleri'),
        (turkce_ders_id, 'Paragrafta Düşünceyi Geliştirme Yolları'),
        (turkce_ders_id, 'Paragrafta Yapı'),
        (turkce_ders_id, 'Paragrafta Konu-Ana Düşünce'),
        (turkce_ders_id, 'Paragrafta Yardımcı Düşünce'),
        (turkce_ders_id, 'Ses Bilgisi'),
        (turkce_ders_id, 'Yazım Kuralları'),
        (turkce_ders_id, 'Noktalama İşaretleri'),
        (turkce_ders_id, 'Sözcükte Yapı/Ekler'),
        (turkce_ders_id, 'Sözcük Türleri'),
        (turkce_ders_id, 'İsimler'),
        (turkce_ders_id, 'Zamirler'),
        (turkce_ders_id, 'Sıfatlar'),
        (turkce_ders_id, 'Zarflar'),
        (turkce_ders_id, 'Edat – Bağlaç – Ünlem'),
        (turkce_ders_id, 'Fiiller'),
        (turkce_ders_id, 'Fiilde Anlam (Kip-Kişi-Yapı)'),
        (turkce_ders_id, 'Ek Fiil'),
        (turkce_ders_id, 'Fiilimsi'),
        (turkce_ders_id, 'Fiilde Çatı'),
        (turkce_ders_id, 'Sözcük Grupları'),
        (turkce_ders_id, 'Cümlenin Ögeleri'),
        (turkce_ders_id, 'Cümle Türleri'),
        (turkce_ders_id, 'Anlatım Bozukluğu')
    ON CONFLICT (ders_id, ad) DO NOTHING;
    
    RAISE NOTICE '✅ TYT Türkçe konuları eklendi/güncellendi (Toplam: 29 konu)';
END $$;

