-- ============================================
-- TÜM TYT KONU GÜNCELLEME MİGRATİON'LARI
-- Bu dosyayı manuel olarak çalıştırabilirsiniz:
-- psql -h 127.0.0.1 -U myappuser -d myappdb -f manual_run_all_tyt_migrations.sql
-- ============================================

-- Önce ders listesini göster
\echo '=== Mevcut Dersler ==='
SELECT id, ad FROM ders ORDER BY id;

\echo ''
\echo '=== Migration V34: Matematik ==='

-- Matematik dersinin konularını TYT konularıyla güncelle
DO $$
DECLARE
    matematik_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    SELECT id INTO matematik_ders_id FROM ders WHERE ad = 'Matematik';
    
    IF matematik_ders_id IS NULL THEN
        RAISE EXCEPTION 'Matematik dersi bulunamadı';
    END IF;
    
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = matematik_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (matematik_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Matematik ders ID: %', matematik_ders_id;
    
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = matematik_ders_id AND ad != 'Genel'
    LOOP
        SELECT COUNT(*) INTO soru_count 
        FROM soru_konu 
        WHERE konu_id = konu_rec.id;
        
        RAISE NOTICE 'Konu: "%" (ID: %) - Bağlı soru sayısı: %', konu_rec.ad, konu_rec.id, soru_count;
        
        IF soru_count > 0 THEN
            UPDATE soru_konu 
            SET konu_id = genel_konu_id 
            WHERE konu_id = konu_rec.id 
            AND soru_id NOT IN (
                SELECT soru_id FROM soru_konu WHERE konu_id = genel_konu_id
            );
            
            GET DIAGNOSTICS tasinan_soru_count = ROW_COUNT;
            RAISE NOTICE '  ✅ % soru "Genel" konusuna taşındı', tasinan_soru_count;
        END IF;
        
        DELETE FROM konu WHERE id = konu_rec.id;
        RAISE NOTICE '  ✅ Konu silindi: "%"', konu_rec.ad;
    END LOOP;
    
    INSERT INTO konu (ders_id, ad) VALUES
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

\echo ''
\echo '=== Migration V35: Türkçe ==='

-- Türkçe dersinin konularını TYT konularıyla güncelle
DO $$
DECLARE
    turkce_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    SELECT id INTO turkce_ders_id FROM ders WHERE ad = 'Türkçe';
    
    IF turkce_ders_id IS NULL THEN
        RAISE EXCEPTION 'Türkçe dersi bulunamadı';
    END IF;
    
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = turkce_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (turkce_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Türkçe ders ID: %', turkce_ders_id;
    
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = turkce_ders_id AND ad != 'Genel'
    LOOP
        SELECT COUNT(*) INTO soru_count 
        FROM soru_konu 
        WHERE konu_id = konu_rec.id;
        
        RAISE NOTICE 'Konu: "%" (ID: %) - Bağlı soru sayısı: %', konu_rec.ad, konu_rec.id, soru_count;
        
        IF soru_count > 0 THEN
            UPDATE soru_konu 
            SET konu_id = genel_konu_id 
            WHERE konu_id = konu_rec.id 
            AND soru_id NOT IN (
                SELECT soru_id FROM soru_konu WHERE konu_id = genel_konu_id
            );
            
            GET DIAGNOSTICS tasinan_soru_count = ROW_COUNT;
            RAISE NOTICE '  ✅ % soru "Genel" konusuna taşındı', tasinan_soru_count;
        END IF;
        
        DELETE FROM konu WHERE id = konu_rec.id;
        RAISE NOTICE '  ✅ Konu silindi: "%"', konu_rec.ad;
    END LOOP;
    
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

\echo ''
\echo '=== Migration V36: Fizik ==='

-- Fizik dersinin konularını TYT konularıyla güncelle
DO $$
DECLARE
    fizik_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    SELECT id INTO fizik_ders_id FROM ders WHERE ad = 'Fizik';
    
    IF fizik_ders_id IS NULL THEN
        RAISE EXCEPTION 'Fizik dersi bulunamadı';
    END IF;
    
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = fizik_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (fizik_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Fizik ders ID: %', fizik_ders_id;
    
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = fizik_ders_id AND ad != 'Genel'
    LOOP
        SELECT COUNT(*) INTO soru_count 
        FROM soru_konu 
        WHERE konu_id = konu_rec.id;
        
        RAISE NOTICE 'Konu: "%" (ID: %) - Bağlı soru sayısı: %', konu_rec.ad, konu_rec.id, soru_count;
        
        IF soru_count > 0 THEN
            UPDATE soru_konu 
            SET konu_id = genel_konu_id 
            WHERE konu_id = konu_rec.id 
            AND soru_id NOT IN (
                SELECT soru_id FROM soru_konu WHERE konu_id = genel_konu_id
            );
            
            GET DIAGNOSTICS tasinan_soru_count = ROW_COUNT;
            RAISE NOTICE '  ✅ % soru "Genel" konusuna taşındı', tasinan_soru_count;
        END IF;
        
        DELETE FROM konu WHERE id = konu_rec.id;
        RAISE NOTICE '  ✅ Konu silindi: "%"', konu_rec.ad;
    END LOOP;
    
    INSERT INTO konu (ders_id, ad) VALUES
        (fizik_ders_id, 'Fizik Bilimine Giriş'),
        (fizik_ders_id, 'Madde ve Özellikleri'),
        (fizik_ders_id, 'Sıvıların Kaldırma Kuvveti'),
        (fizik_ders_id, 'Basınç'),
        (fizik_ders_id, 'Isı, Sıcaklık ve Genleşme'),
        (fizik_ders_id, 'Hareket ve Kuvvet'),
        (fizik_ders_id, 'Dinamik'),
        (fizik_ders_id, 'İş, Güç ve Enerji'),
        (fizik_ders_id, 'Elektrik'),
        (fizik_ders_id, 'Manyetizma'),
        (fizik_ders_id, 'Dalgalar'),
        (fizik_ders_id, 'Optik')
    ON CONFLICT (ders_id, ad) DO NOTHING;
    
    RAISE NOTICE '✅ TYT Fizik konuları eklendi/güncellendi (Toplam: 12 konu)';
END $$;

\echo ''
\echo '=== Migration V37: Biyoloji ==='

-- Biyoloji dersinin konularını TYT konularıyla güncelle
DO $$
DECLARE
    biyoloji_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    SELECT id INTO biyoloji_ders_id FROM ders WHERE ad = 'Biyoloji';
    
    IF biyoloji_ders_id IS NULL THEN
        RAISE EXCEPTION 'Biyoloji dersi bulunamadı';
    END IF;
    
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = biyoloji_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (biyoloji_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Biyoloji ders ID: %', biyoloji_ders_id;
    
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = biyoloji_ders_id AND ad != 'Genel'
    LOOP
        SELECT COUNT(*) INTO soru_count 
        FROM soru_konu 
        WHERE konu_id = konu_rec.id;
        
        RAISE NOTICE 'Konu: "%" (ID: %) - Bağlı soru sayısı: %', konu_rec.ad, konu_rec.id, soru_count;
        
        IF soru_count > 0 THEN
            UPDATE soru_konu 
            SET konu_id = genel_konu_id 
            WHERE konu_id = konu_rec.id 
            AND soru_id NOT IN (
                SELECT soru_id FROM soru_konu WHERE konu_id = genel_konu_id
            );
            
            GET DIAGNOSTICS tasinan_soru_count = ROW_COUNT;
            RAISE NOTICE '  ✅ % soru "Genel" konusuna taşındı', tasinan_soru_count;
        END IF;
        
        DELETE FROM konu WHERE id = konu_rec.id;
        RAISE NOTICE '  ✅ Konu silindi: "%"', konu_rec.ad;
    END LOOP;
    
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

\echo ''
\echo '=== Migration V38: Tarih ==='

-- Tarih dersinin konularını TYT konularıyla güncelle
DO $$
DECLARE
    tarih_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    SELECT id INTO tarih_ders_id FROM ders WHERE ad = 'Tarih';
    
    IF tarih_ders_id IS NULL THEN
        RAISE EXCEPTION 'Tarih dersi bulunamadı';
    END IF;
    
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = tarih_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (tarih_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Tarih ders ID: %', tarih_ders_id;
    
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = tarih_ders_id AND ad != 'Genel'
    LOOP
        SELECT COUNT(*) INTO soru_count 
        FROM soru_konu 
        WHERE konu_id = konu_rec.id;
        
        RAISE NOTICE 'Konu: "%" (ID: %) - Bağlı soru sayısı: %', konu_rec.ad, konu_rec.id, soru_count;
        
        IF soru_count > 0 THEN
            UPDATE soru_konu 
            SET konu_id = genel_konu_id 
            WHERE konu_id = konu_rec.id 
            AND soru_id NOT IN (
                SELECT soru_id FROM soru_konu WHERE konu_id = genel_konu_id
            );
            
            GET DIAGNOSTICS tasinan_soru_count = ROW_COUNT;
            RAISE NOTICE '  ✅ % soru "Genel" konusuna taşındı', tasinan_soru_count;
        END IF;
        
        DELETE FROM konu WHERE id = konu_rec.id;
        RAISE NOTICE '  ✅ Konu silindi: "%"', konu_rec.ad;
    END LOOP;
    
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

\echo ''
\echo '=== Migration V39: Coğrafya ==='

-- Coğrafya dersinin konularını TYT konularıyla güncelle
DO $$
DECLARE
    cografya_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    SELECT id INTO cografya_ders_id FROM ders WHERE ad = 'Coğrafya';
    
    IF cografya_ders_id IS NULL THEN
        RAISE EXCEPTION 'Coğrafya dersi bulunamadı';
    END IF;
    
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = cografya_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (cografya_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Coğrafya ders ID: %', cografya_ders_id;
    
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = cografya_ders_id AND ad != 'Genel'
    LOOP
        SELECT COUNT(*) INTO soru_count 
        FROM soru_konu 
        WHERE konu_id = konu_rec.id;
        
        RAISE NOTICE 'Konu: "%" (ID: %) - Bağlı soru sayısı: %', konu_rec.ad, konu_rec.id, soru_count;
        
        IF soru_count > 0 THEN
            UPDATE soru_konu 
            SET konu_id = genel_konu_id 
            WHERE konu_id = konu_rec.id 
            AND soru_id NOT IN (
                SELECT soru_id FROM soru_konu WHERE konu_id = genel_konu_id
            );
            
            GET DIAGNOSTICS tasinan_soru_count = ROW_COUNT;
            RAISE NOTICE '  ✅ % soru "Genel" konusuna taşındı', tasinan_soru_count;
        END IF;
        
        DELETE FROM konu WHERE id = konu_rec.id;
        RAISE NOTICE '  ✅ Konu silindi: "%"', konu_rec.ad;
    END LOOP;
    
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

\echo ''
\echo '=== Migration V40: Felsefe ==='

-- Felsefe dersinin konularını TYT konularıyla güncelle
DO $$
DECLARE
    felsefe_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    SELECT id INTO felsefe_ders_id FROM ders WHERE ad = 'Felsefe';
    
    IF felsefe_ders_id IS NULL THEN
        RAISE EXCEPTION 'Felsefe dersi bulunamadı';
    END IF;
    
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = felsefe_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (felsefe_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Felsefe ders ID: %', felsefe_ders_id;
    
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = felsefe_ders_id AND ad != 'Genel'
    LOOP
        SELECT COUNT(*) INTO soru_count 
        FROM soru_konu 
        WHERE konu_id = konu_rec.id;
        
        RAISE NOTICE 'Konu: "%" (ID: %) - Bağlı soru sayısı: %', konu_rec.ad, konu_rec.id, soru_count;
        
        IF soru_count > 0 THEN
            UPDATE soru_konu 
            SET konu_id = genel_konu_id 
            WHERE konu_id = konu_rec.id 
            AND soru_id NOT IN (
                SELECT soru_id FROM soru_konu WHERE konu_id = genel_konu_id
            );
            
            GET DIAGNOSTICS tasinan_soru_count = ROW_COUNT;
            RAISE NOTICE '  ✅ % soru "Genel" konusuna taşındı', tasinan_soru_count;
        END IF;
        
        DELETE FROM konu WHERE id = konu_rec.id;
        RAISE NOTICE '  ✅ Konu silindi: "%"', konu_rec.ad;
    END LOOP;
    
    INSERT INTO konu (ders_id, ad) VALUES
        (felsefe_ders_id, 'Felsefe''nin Konusu'),
        (felsefe_ders_id, 'Bilgi Felsefesi'),
        (felsefe_ders_id, 'Varlık Felsefesi'),
        (felsefe_ders_id, 'Ahlak Felsefesi'),
        (felsefe_ders_id, 'Sanat Felsefesi'),
        (felsefe_ders_id, 'Din Felsefesi'),
        (felsefe_ders_id, 'Siyaset Felsefesi'),
        (felsefe_ders_id, 'Bilim Felsefesi'),
        (felsefe_ders_id, 'İlk Çağ Felsefesi'),
        (felsefe_ders_id, '2. Yüzyıl ve 15. Yüzyıl Felsefeleri'),
        (felsefe_ders_id, '15. Yüzyıl ve 17. Yüzyıl Felsefeleri'),
        (felsefe_ders_id, '18. Yüzyıl ve 19. Yüzyıl Felsefeleri'),
        (felsefe_ders_id, '20. Yüzyıl Felsefesi')
    ON CONFLICT (ders_id, ad) DO NOTHING;
    
    RAISE NOTICE '✅ TYT Felsefe konuları eklendi/güncellendi (Toplam: 13 konu)';
END $$;

\echo ''
\echo '=== Migration V41: Din Kültürü ==='

-- Din Kültürü ve Ahlak Bilgisi dersinin konularını TYT konularıyla güncelle
DO $$
DECLARE
    din_kulturu_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    SELECT id INTO din_kulturu_ders_id FROM ders WHERE ad = 'Din Kültürü' OR ad = 'Din Kültürü ve Ahlak Bilgisi';
    
    IF din_kulturu_ders_id IS NULL THEN
        RAISE EXCEPTION 'Din Kültürü dersi bulunamadı';
    END IF;
    
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = din_kulturu_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (din_kulturu_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Din Kültürü ders ID: %', din_kulturu_ders_id;
    
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = din_kulturu_ders_id AND ad != 'Genel'
    LOOP
        SELECT COUNT(*) INTO soru_count 
        FROM soru_konu 
        WHERE konu_id = konu_rec.id;
        
        RAISE NOTICE 'Konu: "%" (ID: %) - Bağlı soru sayısı: %', konu_rec.ad, konu_rec.id, soru_count;
        
        IF soru_count > 0 THEN
            UPDATE soru_konu 
            SET konu_id = genel_konu_id 
            WHERE konu_id = konu_rec.id 
            AND soru_id NOT IN (
                SELECT soru_id FROM soru_konu WHERE konu_id = genel_konu_id
            );
            
            GET DIAGNOSTICS tasinan_soru_count = ROW_COUNT;
            RAISE NOTICE '  ✅ % soru "Genel" konusuna taşındı', tasinan_soru_count;
        END IF;
        
        DELETE FROM konu WHERE id = konu_rec.id;
        RAISE NOTICE '  ✅ Konu silindi: "%"', konu_rec.ad;
    END LOOP;
    
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

\echo ''
\echo '=== Migration V42: Kimya ==='

-- Kimya dersinin konularını TYT konularıyla güncelle
DO $$
DECLARE
    kimya_ders_id BIGINT;
    genel_konu_id BIGINT;
    konu_rec RECORD;
    soru_count BIGINT;
    tasinan_soru_count BIGINT;
BEGIN
    SELECT id INTO kimya_ders_id FROM ders WHERE ad = 'Kimya';
    
    IF kimya_ders_id IS NULL THEN
        RAISE EXCEPTION 'Kimya dersi bulunamadı';
    END IF;
    
    SELECT id INTO genel_konu_id FROM konu WHERE ders_id = kimya_ders_id AND ad = 'Genel';
    
    IF genel_konu_id IS NULL THEN
        INSERT INTO konu (ders_id, ad) VALUES (kimya_ders_id, 'Genel') RETURNING id INTO genel_konu_id;
        RAISE NOTICE '✅ "Genel" konusu oluşturuldu (ID: %)', genel_konu_id;
    ELSE
        RAISE NOTICE '✅ "Genel" konusu bulundu (ID: %)', genel_konu_id;
    END IF;
    
    RAISE NOTICE 'Kimya ders ID: %', kimya_ders_id;
    
    FOR konu_rec IN 
        SELECT id, ad FROM konu WHERE ders_id = kimya_ders_id AND ad != 'Genel'
    LOOP
        SELECT COUNT(*) INTO soru_count 
        FROM soru_konu 
        WHERE konu_id = konu_rec.id;
        
        RAISE NOTICE 'Konu: "%" (ID: %) - Bağlı soru sayısı: %', konu_rec.ad, konu_rec.id, soru_count;
        
        IF soru_count > 0 THEN
            UPDATE soru_konu 
            SET konu_id = genel_konu_id 
            WHERE konu_id = konu_rec.id 
            AND soru_id NOT IN (
                SELECT soru_id FROM soru_konu WHERE konu_id = genel_konu_id
            );
            
            GET DIAGNOSTICS tasinan_soru_count = ROW_COUNT;
            RAISE NOTICE '  ✅ % soru "Genel" konusuna taşındı', tasinan_soru_count;
        END IF;
        
        DELETE FROM konu WHERE id = konu_rec.id;
        RAISE NOTICE '  ✅ Konu silindi: "%"', konu_rec.ad;
    END LOOP;
    
    INSERT INTO konu (ders_id, ad) VALUES
        (kimya_ders_id, 'Kimya Bilimi'),
        (kimya_ders_id, 'Atom ve Periyodik Sistem'),
        (kimya_ders_id, 'Kimyasal Türler Arası Etkileşimler'),
        (kimya_ders_id, 'Maddenin Halleri'),
        (kimya_ders_id, 'Doğa ve Kimya'),
        (kimya_ders_id, 'Kimyanın Temel Kanunları'),
        (kimya_ders_id, 'Kimyasal Hesaplamalar'),
        (kimya_ders_id, 'Karışımlar'),
        (kimya_ders_id, 'Asit, Baz ve Tuz'),
        (kimya_ders_id, 'Kimya Her Yerde')
    ON CONFLICT (ders_id, ad) DO NOTHING;
    
    RAISE NOTICE '✅ TYT Kimya konuları eklendi/güncellendi (Toplam: 10 konu)';
END $$;

\echo ''
\echo '=== Sonuç: Güncel Konu Sayıları ==='
SELECT d.id, d.ad, COUNT(k.id) as konu_sayisi 
FROM ders d 
LEFT JOIN konu k ON d.id = k.ders_id 
GROUP BY d.id, d.ad 
ORDER BY d.id;





