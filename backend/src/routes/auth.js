const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const jwt = require('jsonwebtoken');
const User = require('../models/User');

// Generate JWT token
const generateToken = (userId) => {
  return jwt.sign({ userId }, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN || '30d'
  });
};

// Register/Login with device ID
router.post('/register', async (req, res) => {
  try {
    const { deviceId, fcmToken } = req.body;

    if (!deviceId) {
      return res.status(400).json({ error: 'Device ID is required' });
    }

    // Generate unique user code
    const generateUserCode = () => {
      const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
      let code = '';
      for (let i = 0; i < 8; i++) {
        code += chars.charAt(Math.floor(Math.random() * chars.length));
      }
      return `@${code}`;
    };

    // Find or create user
    let [user, created] = await User.findOrCreate({
      where: { deviceId },
      defaults: {
        id: uuidv4(),
        deviceId,
        fcmToken: fcmToken || null,
        userCode: null // Will generate below
      }
    });

    // Generate user code if not exists
    if (!user.userCode) {
      let userCode = generateUserCode();
      let existingUser = await User.findOne({ where: { userCode } });
      let attempts = 0;
      while (existingUser && attempts < 10) {
        userCode = generateUserCode();
        existingUser = await User.findOne({ where: { userCode } });
        if (!existingUser) break;
        attempts++;
      }
      user.userCode = userCode;
      await user.save();
    }

    // Update FCM token if provided
    if (fcmToken && user.fcmToken !== fcmToken) {
      user.fcmToken = fcmToken;
      await user.save();
    }

    const token = generateToken(user.id);

    res.json({
      success: true,
      token,
      user: {
        id: user.id,
        deviceId: user.deviceId,
        userCode: user.userCode,
        pairId: user.pairId,
        premium: user.premium
      }
    });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
