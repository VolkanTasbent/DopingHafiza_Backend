-- Tüm TYT konu güncelleme migration'larını manuel çalıştır
-- Bu script'i psql ile çalıştırabilirsiniz: psql -h 127.0.0.1 -U myappuser -d myappdb -f run_all_tyt_migrations.sql

-- Önce ders adlarını kontrol et
\echo '=== Ders Listesi ==='
SELECT id, ad FROM ders ORDER BY id;

\echo ''
\echo '=== Migration V34: Matematik ==='
\i src/main/resources/db/migration/V34__update_matematik_konulari_tyt.sql

\echo ''
\echo '=== Migration V35: Türkçe ==='
\i src/main/resources/db/migration/V35__update_turkce_konulari_tyt.sql

\echo ''
\echo '=== Migration V36: Fizik ==='
\i src/main/resources/db/migration/V36__update_fizik_konulari_tyt.sql

\echo ''
\echo '=== Migration V37: Biyoloji ==='
\i src/main/resources/db/migration/V37__update_biyoloji_konulari_tyt.sql

\echo ''
\echo '=== Migration V38: Tarih ==='
\i src/main/resources/db/migration/V38__update_tarih_konulari_tyt.sql

\echo ''
\echo '=== Migration V39: Coğrafya ==='
\i src/main/resources/db/migration/V39__update_cografya_konulari_tyt.sql

\echo ''
\echo '=== Migration V40: Felsefe ==='
\i src/main/resources/db/migration/V40__update_felsefe_konulari_tyt.sql

\echo ''
\echo '=== Migration V41: Din Kültürü ==='
\i src/main/resources/db/migration/V41__update_din_kulturu_konulari_tyt.sql

\echo ''
\echo '=== Migration V42: Kimya ==='
\i src/main/resources/db/migration/V42__update_kimya_konulari_tyt.sql

\echo ''
\echo '=== Sonuç: Güncel Konu Sayıları ==='
SELECT d.id, d.ad, COUNT(k.id) as konu_sayisi 
FROM ders d 
LEFT JOIN konu k ON d.id = k.ders_id 
GROUP BY d.id, d.ad 
ORDER BY d.id;





