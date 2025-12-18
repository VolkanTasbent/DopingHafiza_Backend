-- Soruları CSV formatında export et
-- PostgreSQL'de çalıştırın: psql -d myappdb -U myappuser -f export_sorular_to_csv.sql

COPY (
    SELECT 
        s.id AS soru_id,
        d.ad AS ders_ad,
        k.ad AS konu_ad,
        s.metin AS soru_metni,
        s.tip AS soru_tipi,
        s.zorluk,
        s.soru_no,
        s.aciklama,
        s.image_url,
        s.cozum_videosu_url,
        -- Seçenekleri birleştir
        STRING_AGG(
            CASE 
                WHEN sec.siralama = 1 THEN 'A) ' || sec.metin
                WHEN sec.siralama = 2 THEN 'B) ' || sec.metin
                WHEN sec.siralama = 3 THEN 'C) ' || sec.metin
                WHEN sec.siralama = 4 THEN 'D) ' || sec.metin
                WHEN sec.siralama = 5 THEN 'E) ' || sec.metin
                ELSE sec.metin
            END,
            ' | '
            ORDER BY sec.siralama
        ) AS secenekler,
        -- Doğru cevabı bul
        STRING_AGG(
            CASE 
                WHEN sec.dogru = true THEN
                    CASE sec.siralama
                        WHEN 1 THEN 'A'
                        WHEN 2 THEN 'B'
                        WHEN 3 THEN 'C'
                        WHEN 4 THEN 'D'
                        WHEN 5 THEN 'E'
                    END
                ELSE NULL
            END,
            ''
        ) AS dogru_cevap
    FROM soru s
    INNER JOIN ders d ON s.ders_id = d.id
    INNER JOIN soru_konu sk ON s.id = sk.soru_id
    INNER JOIN konu k ON sk.konu_id = k.id
    LEFT JOIN secenek sec ON s.id = sec.soru_id
    WHERE k.ad != 'Genel'
    GROUP BY s.id, d.ad, k.ad, s.metin, s.tip, s.zorluk, s.soru_no, s.aciklama, s.image_url, s.cozum_videosu_url
    ORDER BY d.ad, k.ad, s.soru_no
) TO STDOUT WITH CSV HEADER;




