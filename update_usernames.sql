-- Username kaldırma SQL script'i
-- Bu script, mevcut kullanıcıların username alanlarını email ile değiştirir

-- 1. Önce mevcut durumu kontrol et
SELECT id, username, email FROM users WHERE username != email;

-- 2. Tüm kullanıcıların username'ini email ile güncelle
UPDATE users SET username = email WHERE username != email;

-- 3. Sonucu doğrula
SELECT id, username, email FROM users;
