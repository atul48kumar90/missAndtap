-- Migration: Add user code and whitelist system
-- Date: 2024

-- Add userCode to users table
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS "userCode" VARCHAR(20) UNIQUE;

-- Create index for userCode
CREATE INDEX IF NOT EXISTS idx_users_user_code ON users("userCode");

-- Generate user codes for existing users (if any)
-- This will be handled by the application on next login

-- Create allowed_tappers table
CREATE TABLE IF NOT EXISTS "allowed_tappers" (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  "userId" UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  "tapperUserId" UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  "tapperUserCode" VARCHAR(20) NOT NULL,
  "createdAt" TIMESTAMP DEFAULT NOW(),
  UNIQUE("userId", "tapperUserId")
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_allowed_tappers_user_id ON "allowed_tappers"("userId");
CREATE INDEX IF NOT EXISTS idx_allowed_tappers_tapper_user_id ON "allowed_tappers"("tapperUserId");
CREATE INDEX IF NOT EXISTS idx_allowed_tappers_tapper_user_code ON "allowed_tappers"("tapperUserCode");
