-- Eski kayıtların puanlarını YKS net sistemine göre yeniden hesapla

-- 1. Empty değerlerini hesapla (eğer NULL ise)
UPDATE quiz_oturumu 
SET empty = COALESCE(total, 0) - COALESCE(correct, 0) - COALESCE(wrong, 0)
WHERE empty IS NULL;

-- 2. Score değerlerini YKS net sistemine göre yeniden hesapla
-- Net = Doğru - (Yanlış / 4), yuvarlanmış hali score olarak saklanır
UPDATE quiz_oturumu 
SET score = ROUND(COALESCE(correct, 0) - (COALESCE(wrong, 0)::numeric / 4.0))
WHERE score IS NULL OR score = 0 OR score != ROUND(COALESCE(correct, 0) - (COALESCE(wrong, 0)::numeric / 4.0));

-- 3. NULL değerleri 0 yap
UPDATE quiz_oturumu 
SET empty = 0
WHERE empty IS NULL;

UPDATE quiz_oturumu 
SET score = 0
WHERE score IS NULL;

