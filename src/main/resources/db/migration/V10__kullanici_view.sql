DROP VIEW IF EXISTS kullanici;

CREATE VIEW kullanici AS
SELECT
    u.id,
    u.ad,
    u.soyad,
    u.email,
    u.role   AS rol,
    u.enabled AS aktif,
    u.created_at,
    u.updated_at
FROM app_user u;
