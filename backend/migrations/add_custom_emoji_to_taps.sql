-- Migration: Add customEmoji column to taps table
-- Date: 2024

ALTER TABLE taps 
ADD COLUMN IF NOT EXISTS "customEmoji" VARCHAR(50);

-- No index needed for custom emoji (optional field, not frequently queried)
