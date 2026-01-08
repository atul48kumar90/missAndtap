const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const Pair = require('../models/Pair');
const User = require('../models/User');
const auth = require('../middleware/auth');

// Generate random 6-character invite code
const generateInviteCode = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  let code = '';
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
};

// Create pair with invite code
router.post('/create', auth, async (req, res) => {
  try {
    const userId = req.userId;
    const code = generateInviteCode();

    // Check if code already exists (very unlikely, but check anyway)
    let existingPair = await Pair.findByPk(code);
    let attempts = 0;
    while (existingPair && attempts < 10) {
      const newCode = generateInviteCode();
      existingPair = await Pair.findByPk(newCode);
      if (!existingPair) {
        code = newCode;
        break;
      }
      attempts++;
    }

    const pair = await Pair.create({
      id: code,
      user1Id: userId,
      user2Id: null
    });

    // Update user's pairId
    await User.update({ pairId: code }, { where: { id: userId } });

    res.json({
      success: true,
      inviteCode: code,
      pair: {
        id: pair.id,
        user1Id: pair.user1Id,
        user2Id: pair.user2Id
      }
    });
  } catch (error) {
    console.error('Create pair error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Join pair with invite code
router.post('/join', auth, async (req, res) => {
  try {
    const userId = req.userId;
    const { inviteCode } = req.body;

    if (!inviteCode || inviteCode.length !== 6) {
      return res.status(400).json({ error: 'Invalid invite code' });
    }

    const pair = await Pair.findByPk(inviteCode.toUpperCase());

    if (!pair) {
      return res.status(404).json({ error: 'Pair not found' });
    }

    if (pair.user2Id) {
      return res.status(400).json({ error: 'Pair already full' });
    }

    if (pair.user1Id === userId) {
      return res.status(400).json({ error: 'Cannot join your own pair' });
    }

    // Join the pair
    pair.user2Id = userId;
    await pair.save();

    // Update user's pairId
    await User.update({ pairId: inviteCode.toUpperCase() }, { where: { id: userId } });

    // Invalidate cache
    const cacheService = require('../services/cacheService');
    await cacheService.invalidatePair(inviteCode.toUpperCase());

    res.json({
      success: true,
      pair: {
        id: pair.id,
        user1Id: pair.user1Id,
        user2Id: pair.user2Id
      }
    });
  } catch (error) {
    console.error('Join pair error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get pair info
router.get('/:pairId', auth, async (req, res) => {
  try {
    const { pairId } = req.params;
    const userId = req.userId;

    // Try cache first
    const cacheService = require('../services/cacheService');
    let pair = await cacheService.getPair(pairId);
    
    if (!pair) {
      pair = await Pair.findByPk(pairId);
      if (pair) {
        // Cache for 5 minutes
        await cacheService.setPair(pairId, pair, 300);
      }
    }

    if (!pair) {
      return res.status(404).json({ error: 'Pair not found' });
    }

    if (pair.user1Id !== userId && pair.user2Id !== userId) {
      return res.status(403).json({ error: 'Not authorized' });
    }

    res.json({
      success: true,
      pair: {
        id: pair.id,
        user1Id: pair.user1Id,
        user2Id: pair.user2Id,
        lastTapAt: pair.lastTapAt,
        lastTapAtReverse: pair.lastTapAtReverse,
        streak: pair.streak || 0,
        lastStreakDate: pair.lastStreakDate,
        createdAt: pair.createdAt
      }
    });
  } catch (error) {
    console.error('Get pair error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
