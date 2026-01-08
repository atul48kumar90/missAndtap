const express = require('express');
const router = express.Router();
const User = require('../models/User');
const AllowedTapper = require('../models/AllowedTapper');
const auth = require('../middleware/auth');

// Get my user code
router.get('/my-code', auth, async (req, res) => {
  try {
    const userId = req.userId;
    const user = await User.findByPk(userId);
    
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    res.json({
      success: true,
      userCode: user.userCode
    });
  } catch (error) {
    console.error('Get user code error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Regenerate user code
router.post('/regenerate-code', auth, async (req, res) => {
  try {
    const userId = req.userId;
    const user = await User.findByPk(userId);
    
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    // Generate new unique code
    const generateUserCode = () => {
      const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
      let code = '';
      for (let i = 0; i < 8; i++) {
        code += chars.charAt(Math.floor(Math.random() * chars.length));
      }
      return `@${code}`;
    };

    let newCode = generateUserCode();
    let existingUser = await User.findOne({ where: { userCode: newCode } });
    let attempts = 0;
    while (existingUser && attempts < 10) {
      newCode = generateUserCode();
      existingUser = await User.findOne({ where: { userCode: newCode } });
      if (!existingUser) break;
      attempts++;
    }

    // Remove old code from all whitelists
    await AllowedTapper.destroy({
      where: { tapperUserCode: user.userCode }
    });

    // Update user code
    user.userCode = newCode;
    await user.save();

    res.json({
      success: true,
      userCode: newCode,
      message: 'User code regenerated. You need to share your new code with people who want to tap you.'
    });
  } catch (error) {
    console.error('Regenerate code error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get list of allowed tappers (people who can tap me)
router.get('/allowed-tappers', auth, async (req, res) => {
  try {
    const userId = req.userId;
    
    const allowedTappers = await AllowedTapper.findAll({
      where: { userId },
      order: [['createdAt', 'DESC']]
    });

    // Get user details for each tapper
    const tapperDetails = await Promise.all(
      allowedTappers.map(async (tapper) => {
        const tapperUser = await User.findByPk(tapper.tapperUserId, {
          attributes: ['id', 'userCode', 'createdAt']
        });
        return {
          id: tapper.id,
          tapperUserId: tapper.tapperUserId,
          tapperUserCode: tapper.tapperUserCode,
          addedAt: tapper.createdAt,
          user: tapperUser
        };
      })
    );

    res.json({
      success: true,
      allowedTappers: tapperDetails
    });
  } catch (error) {
    console.error('Get allowed tappers error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Add user to allowed tappers list (by user code)
router.post('/add-tapper', auth, async (req, res) => {
  try {
    const userId = req.userId;
    const { userCode } = req.body;

    if (!userCode) {
      return res.status(400).json({ error: 'User code is required' });
    }

    // Validate format
    if (!userCode.startsWith('@') || userCode.length < 7 || userCode.length > 21) {
      return res.status(400).json({ error: 'Invalid user code format' });
    }

    // Find user by code
    const tapperUser = await User.findOne({ where: { userCode: userCode.toUpperCase() } });
    
    if (!tapperUser) {
      return res.status(404).json({ error: 'User code not found' });
    }

    if (tapperUser.id === userId) {
      return res.status(400).json({ error: 'Cannot add yourself' });
    }

    // Check if already added
    const existing = await AllowedTapper.findOne({
      where: {
        userId,
        tapperUserId: tapperUser.id
      }
    });

    if (existing) {
      return res.status(400).json({ error: 'User already in your allowed tappers list' });
    }

    // Add to whitelist
    const allowedTapper = await AllowedTapper.create({
      userId,
      tapperUserId: tapperUser.id,
      tapperUserCode: tapperUser.userCode
    });

    res.json({
      success: true,
      message: 'User added to allowed tappers',
      allowedTapper: {
        id: allowedTapper.id,
        tapperUserId: tapperUser.id,
        tapperUserCode: tapperUser.userCode
      }
    });
  } catch (error) {
    console.error('Add tapper error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Remove user from allowed tappers list
router.delete('/remove-tapper/:tapperId', auth, async (req, res) => {
  try {
    const userId = req.userId;
    const { tapperId } = req.params;

    const allowedTapper = await AllowedTapper.findOne({
      where: {
        id: tapperId,
        userId
      }
    });

    if (!allowedTapper) {
      return res.status(404).json({ error: 'Tapper not found in your list' });
    }

    await allowedTapper.destroy();

    res.json({
      success: true,
      message: 'User removed from allowed tappers'
    });
  } catch (error) {
    console.error('Remove tapper error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Check if a user can tap me (for validation)
router.get('/can-tap/:userCode', auth, async (req, res) => {
  try {
    const userId = req.userId;
    const { userCode } = req.params;

    const tapperUser = await User.findOne({ where: { userCode: userCode.toUpperCase() } });
    
    if (!tapperUser) {
      return res.json({
        success: true,
        canTap: false,
        reason: 'User code not found'
      });
    }

    const allowed = await AllowedTapper.findOne({
      where: {
        userId,
        tapperUserId: tapperUser.id
      }
    });

    res.json({
      success: true,
      canTap: !!allowed,
      reason: allowed ? 'User is in your allowed tappers list' : 'User is not in your allowed tappers list'
    });
  } catch (error) {
    console.error('Check can tap error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

module.exports = router;
