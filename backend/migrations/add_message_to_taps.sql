-- Migration: Add message column to taps table
-- Date: 2024

ALTER TABLE taps 
ADD COLUMN IF NOT EXISTS "message" VARCHAR(100);
