const express = require('express');
const router = express.Router();
const Tap = require('../models/Tap');
const Pair = require('../models/Pair');
const User = require('../models/User');
const auth = require('../middleware/auth');
const pushService = require('../services/pushService');
const rateLimitService = require('../services/rateLimitService');

// Send a tap
router.post('/send', auth, async (req, res) => {
  try {
    const fromUserId = req.userId;
    const { pairId, tapType, customEmoji, message } = req.body;

    if (!pairId) {
      return res.status(400).json({ error: 'Pair ID is required' });
    }

    // Validate custom emoji if provided
    if (customEmoji) {
      if (customEmoji.length > 50) {
        return res.status(400).json({ error: 'Custom emoji must be 50 characters or less' });
      }
      // Basic emoji validation (Unicode check)
      const emojiRegex = /[\u{1F300}-\u{1F9FF}]|[\u{2600}-\u{27FF}]|[\u{2700}-\u{27BF}]/u;
      if (!emojiRegex.test(customEmoji) && customEmoji.length > 0) {
        // Allow if it contains at least some emoji-like characters
        // This is lenient to allow various emoji formats
      }
    }

    // Validate message if provided
    if (message) {
      if (message.length > 100) {
        return res.status(400).json({ error: 'Message must be 100 characters or less' });
      }
    }

    // Validate tap type (default to LOVING_MISS if not provided and no custom emoji)
    const validTapTypes = ['HAPPY_MISS', 'SAD_MISS', 'NAUGHTY_MISS', 'LOVING_MISS', 
                          'EXCITED_MISS', 'SLEEPY_MISS', 'PLAYFUL_MISS', 'THINKING_MISS'];
    const finalTapType = tapType && validTapTypes.includes(tapType) ? tapType : 
                        (customEmoji ? null : 'LOVING_MISS'); // Use custom emoji or default type

    // Get pair
    const pair = await Pair.findByPk(pairId);
    if (!pair) {
      return res.status(404).json({ error: 'Pair not found' });
    }

    if (!pair.user2Id) {
      return res.status(400).json({ error: 'Pair not complete' });
    }

    // Determine recipient
    const toUserId = pair.user1Id === fromUserId ? pair.user2Id : pair.user1Id;

    // Check if sender is in recipient's allowed tappers list
    const AllowedTapper = require('../models/AllowedTapper');
    const isAllowed = await AllowedTapper.findOne({
      where: {
        userId: toUserId,
        tapperUserId: fromUserId
      }
    });

    if (!isAllowed) {
      // Get sender's user code for error message
      const sender = await User.findByPk(fromUserId);
      return res.status(403).json({
        error: 'Not allowed to tap this user',
        message: 'This user has not added you to their allowed tappers list. Share your user code with them first.',
        userCode: sender?.userCode || null
      });
    }

    // Check rate limits (cooldown and daily limit per pair)
    const rateLimit = await rateLimitService.checkTapLimit(pairId, fromUserId);
    if (!rateLimit.allowed) {
      return res.status(429).json({
        error: rateLimit.reason === 'cooldown' ? 'Cooldown period' : 'Daily limit reached',
        reason: rateLimit.reason,
        message: rateLimit.message,
        limit: rateLimit.limit,
        current: rateLimit.current,
        remainingMinutes: rateLimit.remainingMinutes,
        dailyRemaining: rateLimit.dailyRemaining || 0
      });
    }

    // Create tap
    const tap = await Tap.create({
      pairId,
      fromUserId,
      toUserId,
      timestamp: new Date(),
      tapType: finalTapType,
      customEmoji: customEmoji || null,
      message: message || null,
      synced: true
    });

    // Update pair's last tap timestamp and streak
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const lastStreakDate = pair.lastStreakDate ? new Date(pair.lastStreakDate) : null;
    const lastStreakDay = lastStreakDate ? new Date(lastStreakDate.getFullYear(), lastStreakDate.getMonth(), lastStreakDate.getDate()) : null;
    
    if (pair.user1Id === fromUserId) {
      pair.lastTapAt = now;
    } else {
      pair.lastTapAtReverse = now;
    }
    
    // Update streak: increment if tapped today, reset if missed a day
    if (!lastStreakDay || lastStreakDay.getTime() === today.getTime()) {
      // Same day - no change to streak (already counted today)
      if (!pair.lastStreakDate) {
        // First tap ever - start streak
        pair.streak = 1;
        pair.lastStreakDate = today;
      }
    } else {
      const daysDiff = Math.floor((today.getTime() - lastStreakDay.getTime()) / (1000 * 60 * 60 * 24));
      if (daysDiff === 1) {
        // Consecutive day - increment streak
        pair.streak = (pair.streak || 0) + 1;
        pair.lastStreakDate = today;
      } else {
        // Missed a day - reset streak
        pair.streak = 1;
        pair.lastStreakDate = today;
      }
    }
    
    await pair.save();

    // Invalidate cache
    const cacheService = require('../services/cacheService');
    await cacheService.invalidatePair(pairId);
    await cacheService.del(`stats:${pairId}:${fromUserId}`);

    // Get recipient's FCM token
    const recipient = await User.findByPk(toUserId);
    if (recipient && recipient.fcmToken) {
      // Send push notification with tap type (async, don't wait)
      // Include fromUserId so app can show full-screen notification
      pushService.sendTapNotification(recipient.fcmToken, pairId, finalTapType, fromUserId, customEmoji, message)
        .then(result => {
          // Remove invalid tokens
          if (result.shouldRemove) {
            User.update({ fcmToken: null }, { where: { id: toUserId } })
              .catch(err => console.error('Failed to remove invalid token:', err));
          }
        })
        .catch(err => console.error('Push notification failed:', err));
    }

    res.json({
      success: true,
      tap: {
        id: tap.id,
        pairId: tap.pairId,
        timestamp: tap.timestamp
      },
      rateLimit: {
        hourlyRemaining: rateLimit.hourlyRemaining,
        dailyRemaining: rateLimit.dailyRemaining
      }
    });
  } catch (error) {
    console.error('Send tap error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get tap stats
router.get('/stats/:pairId', auth, async (req, res) => {
  try {
    const { pairId } = req.params;
    const userId = req.userId;

    // Verify user belongs to pair
    const pair = await Pair.findByPk(pairId);
    if (!pair || (pair.user1Id !== userId && pair.user2Id !== userId)) {
      return res.status(403).json({ error: 'Not authorized' });
    }

    // Try cache first
    const cacheService = require('../services/cacheService');
    let cachedStats = await cacheService.getTapStats(pairId, userId);
    
    if (cachedStats) {
      return res.json({
        success: true,
        stats: cachedStats
      });
    }

    // Get today's tap count
    const startOfDay = new Date();
    startOfDay.setHours(0, 0, 0, 0);

    const todayTaps = await Tap.count({
      where: {
        pairId,
        fromUserId: userId,
        timestamp: {
          [require('sequelize').Op.gte]: startOfDay
        }
      }
    });

    // Get last tap time
    const lastTap = pair.user1Id === userId ? pair.lastTapAt : pair.lastTapAtReverse;

    const stats = {
      todayTapCount: todayTaps,
      lastTapTime: lastTap,
      streak: pair.streak || 0
    };

    // Cache stats for 60 seconds
    await cacheService.setTapStats(pairId, userId, stats, 60);

    res.json({
      success: true,
      stats: stats
    });
  } catch (error) {
    console.error('Get stats error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
