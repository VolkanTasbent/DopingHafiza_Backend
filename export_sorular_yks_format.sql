-- Soruları CSV formatında export et (soru_csv_sablon.csv formatına uygun)
-- Format: soru_metni,sik_a,sik_b,sik_c,sik_d,sik_e,dogru_cevap,zorluk,konular,ders_ad,aciklama,image_url,cozum_videosu_url
-- PostgreSQL'de çalıştırın: 
-- psql -d myappdb -U myappuser -f export_sorular_yks_format.sql > sorular_yks.csv

\copy (
    SELECT 
        s.metin AS soru_metni,
        MAX(CASE WHEN sec.siralama = 1 THEN sec.metin END) AS sik_a,
        MAX(CASE WHEN sec.siralama = 2 THEN sec.metin END) AS sik_b,
        MAX(CASE WHEN sec.siralama = 3 THEN sec.metin END) AS sik_c,
        MAX(CASE WHEN sec.siralama = 4 THEN sec.metin END) AS sik_d,
        MAX(CASE WHEN sec.siralama = 5 THEN sec.metin END) AS sik_e,
        MAX(CASE 
            WHEN sec.dogru = true THEN
                CASE sec.siralama
                    WHEN 1 THEN 'A'
                    WHEN 2 THEN 'B'
                    WHEN 3 THEN 'C'
                    WHEN 4 THEN 'D'
                    WHEN 5 THEN 'E'
                END
        END) AS dogru_cevap,
        COALESCE(s.zorluk::TEXT, '') AS zorluk,
        STRING_AGG(k.ad, ',' ORDER BY k.ad) AS konular,
        d.ad AS ders_ad,
        COALESCE(s.aciklama, '') AS aciklama,
        COALESCE(s.image_url, '') AS image_url,
        COALESCE(s.cozum_videosu_url, '') AS cozum_videosu_url
    FROM soru s
    INNER JOIN ders d ON s.ders_id = d.id
    INNER JOIN soru_konu sk ON s.id = sk.soru_id
    INNER JOIN konu k ON sk.konu_id = k.id
    LEFT JOIN secenek sec ON s.id = sec.soru_id
    WHERE k.ad != 'Genel'
    GROUP BY s.id, s.metin, s.zorluk, d.ad, s.aciklama, s.image_url, s.cozum_videosu_url
    ORDER BY d.ad, k.ad, s.soru_no
) TO STDOUT WITH CSV HEADER;

