-- Username kolonu tamamen kaldırma SQL script'i
-- DİKKAT: Bu script username kolonunu veritabanından siler!

-- 1. Önce username kolonunu kaldır
ALTER TABLE users DROP COLUMN IF EXISTS username;

-- 2. Sonucu doğrula
SELECT column_name FROM information_schema.columns WHERE table_name = 'users';
