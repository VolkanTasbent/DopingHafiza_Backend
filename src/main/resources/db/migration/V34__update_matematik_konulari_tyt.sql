-- Matematik dersinin konularını TYT konularıyla güncelle
-- 1. Mevcut konulara bağlı soruları "Genel" konusuna taşı
-- 2. Tüm matematik konularını sil (Genel hariç)
-- 3. Yeni TYT konularını ekle

DO $$
DECLARE
    matematik_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    -- Matematik ders ID'sini al
    SELECT id INTO matematik_ders_id FROM ders WHERE ad = 'Matematik';
    
    IF matematik_ders_id IS NULL THEN
        RAISE EXCEPTION 'Matematik dersi bulunamadı';
    END IF;
    
    -- "Genel" konusunu bul veya oluştur
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = matematik_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (matematik_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Matematik ders ID: %', matematik_ders_id;
    
    -- Mevcut konuları kontrol et ve soruları "Genel" konusuna taşı
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = matematik_ders_id AND ad != 'Genel'
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
    
    -- Yeni TYT Matematik ve Geometri konularını ekle (sadece yoksa)
    INSERT INTO konu (ders_id, ad) VALUES
        -- Matematik Konuları
        (matematik_ders_id, 'Temel Kavramlar'),
        (matematik_ders_id, 'Sayı Basamakları'),
        (matematik_ders_id, 'Bölme ve Bölünebilme'),
        (matematik_ders_id, 'EBOB – EKOK'),
        (matematik_ders_id, 'Rasyonel Sayılar'),
        (matematik_ders_id, 'Basit Eşitsizlikler'),
        (matematik_ders_id, 'Mutlak Değer'),
        (matematik_ders_id, 'Üslü Sayılar'),
        (matematik_ders_id, 'Köklü Sayılar'),
        (matematik_ders_id, 'Çarpanlara Ayırma'),
        (matematik_ders_id, 'Oran Orantı'),
        (matematik_ders_id, 'Denklem Çözme'),
        (matematik_ders_id, 'Problemler'),
        (matematik_ders_id, 'Sayı Problemleri'),
        (matematik_ders_id, 'Kesir Problemleri'),
        (matematik_ders_id, 'Yaş Problemleri'),
        (matematik_ders_id, 'Hareket Hız Problemleri'),
        (matematik_ders_id, 'İşçi Emek Problemleri'),
        (matematik_ders_id, 'Yüzde Problemleri'),
        (matematik_ders_id, 'Kar Zarar Problemleri'),
        (matematik_ders_id, 'Karışım Problemleri'),
        (matematik_ders_id, 'Grafik Problemleri'),
        (matematik_ders_id, 'Rutin Olmayan Problemleri'),
        (matematik_ders_id, 'Kümeler – Kartezyen Çarpım'),
        (matematik_ders_id, 'Mantık'),
        (matematik_ders_id, 'Fonskiyonlar'),
        (matematik_ders_id, 'Polinomlar'),
        (matematik_ders_id, '2.Dereceden Denklemler'),
        (matematik_ders_id, 'Permütasyon ve Kombinasyon'),
        (matematik_ders_id, 'Olasılık'),
        (matematik_ders_id, 'Veri – İstatistik'),
        -- Geometri Konuları
        (matematik_ders_id, 'Doğruda Açılar'),
        (matematik_ders_id, 'Üçgende Açılar'),
        (matematik_ders_id, 'Özel Üçgenler'),
        (matematik_ders_id, 'Dik Üçgen'),
        (matematik_ders_id, 'İkizkenar Üçgen'),
        (matematik_ders_id, 'Eşkenar Üçgen'),
        (matematik_ders_id, 'Açıortay'),
        (matematik_ders_id, 'Kenarortay'),
        (matematik_ders_id, 'Eşlik ve Benzerlik'),
        (matematik_ders_id, 'Üçgende Alan'),
        (matematik_ders_id, 'Üçgende Benzerlik'),
        (matematik_ders_id, 'Açı Kenar Bağıntıları'),
        (matematik_ders_id, 'Çokgenler'),
        (matematik_ders_id, 'Özel Dörtgenler'),
        (matematik_ders_id, 'Dörtgenler'),
        (matematik_ders_id, 'Deltoid'),
        (matematik_ders_id, 'Paralelkenar'),
        (matematik_ders_id, 'Eşkenar Dörtgen'),
        (matematik_ders_id, 'Dikdörtgen'),
        (matematik_ders_id, 'Kare'),
        (matematik_ders_id, 'Yamuk'),
        (matematik_ders_id, 'Çember ve Daire'),
        (matematik_ders_id, 'Çemberde Açı'),
        (matematik_ders_id, 'Çemberde Uzun'),
        (matematik_ders_id, 'Dairede Çevre ve Alan'),
        (matematik_ders_id, 'Analitik Geometri'),
        (matematik_ders_id, 'Noktanın Analitiği'),
        (matematik_ders_id, 'Doğrunun Analitiği'),
        (matematik_ders_id, 'Dönüşüm Geometrisi'),
        (matematik_ders_id, 'Katı Cisimler'),
        (matematik_ders_id, 'Prizmalar'),
        (matematik_ders_id, 'Küp'),
        (matematik_ders_id, 'Silindir'),
        (matematik_ders_id, 'Piramit'),
        (matematik_ders_id, 'Koni'),
        (matematik_ders_id, 'Küre'),
        (matematik_ders_id, 'Çemberin Analitiği')
    ON CONFLICT (ders_id, ad) DO NOTHING;
    
    RAISE NOTICE '✅ TYT Matematik ve Geometri konuları eklendi/güncellendi (Toplam: 62 konu)';
END $$;

