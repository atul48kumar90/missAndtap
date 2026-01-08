const Tap = require('../models/Tap');

class RateLimitService {
  async checkTapLimit(pairId, fromUserId) {
    // Rate limits per pair: 5 taps per day, 1 hour cooldown between taps
    // This creates scarcity and makes each tap feel special
    const now = new Date();
    const oneDayAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000);
    const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000);

    // Count taps in last day for this pair
    const dailyTaps = await Tap.count({
      where: {
        pairId,
        fromUserId,
        timestamp: {
          [require('sequelize').Op.gte]: oneDayAgo
        }
      }
    });

    // Get last tap time for this pair (for cooldown)
    const lastTap = await Tap.findOne({
      where: {
        pairId,
        fromUserId
      },
      order: [['timestamp', 'DESC']]
    });

    const DAILY_LIMIT_PER_PAIR = 5; // Creates scarcity - makes each tap special
    const COOLDOWN_MINUTES = 60; // 1 hour cooldown between taps

    // Check cooldown
    if (lastTap && lastTap.timestamp) {
      const timeSinceLastTap = (now.getTime() - new Date(lastTap.timestamp).getTime()) / (1000 * 60); // minutes
      if (timeSinceLastTap < COOLDOWN_MINUTES) {
        const remainingMinutes = Math.ceil(COOLDOWN_MINUTES - timeSinceLastTap);
        return {
          allowed: false,
          reason: 'cooldown',
          message: `Wait ${remainingMinutes} minute${remainingMinutes > 1 ? 's' : ''} before your next tap. Make it count! ❤️`,
          remainingMinutes: remainingMinutes,
          dailyRemaining: Math.max(0, DAILY_LIMIT_PER_PAIR - dailyTaps)
        };
      }
    }

    // Check daily limit
    if (dailyTaps >= DAILY_LIMIT_PER_PAIR) {
      return {
        allowed: false,
        reason: 'daily_limit',
        message: `You've sent all ${DAILY_LIMIT_PER_PAIR} taps for today. Tomorrow is a new day! ❤️`,
        limit: DAILY_LIMIT_PER_PAIR,
        current: dailyTaps,
        dailyRemaining: 0
      };
    }

    return {
      allowed: true,
      dailyRemaining: DAILY_LIMIT_PER_PAIR - dailyTaps,
      cooldownMinutes: COOLDOWN_MINUTES
    };
  }
}

module.exports = new RateLimitService();
