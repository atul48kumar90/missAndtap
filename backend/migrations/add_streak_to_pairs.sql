-- Migration: Add streak tracking to pairs table
-- Date: 2024

ALTER TABLE pairs 
ADD COLUMN IF NOT EXISTS "streak" INTEGER DEFAULT 0;

ALTER TABLE pairs 
ADD COLUMN IF NOT EXISTS "lastStreakDate" DATE;
