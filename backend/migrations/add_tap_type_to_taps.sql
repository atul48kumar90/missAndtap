-- Migration: Add tapType column to taps table
-- Date: 2024

ALTER TABLE taps 
ADD COLUMN IF NOT EXISTS "tapType" VARCHAR(50) DEFAULT 'LOVING_MISS';

-- Create index for tapType queries
CREATE INDEX IF NOT EXISTS idx_taps_tap_type ON taps("tapType");

-- Update existing taps to have default tap type
UPDATE taps 
SET "tapType" = 'LOVING_MISS' 
WHERE "tapType" IS NULL;
