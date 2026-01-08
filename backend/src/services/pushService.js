const admin = require('firebase-admin');
require('dotenv').config();

// Initialize Firebase Admin SDK
let firebaseInitialized = false;

if (!firebaseInitialized && process.env.FIREBASE_SERVICE_ACCOUNT) {
  try {
    const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    firebaseInitialized = true;
    console.log('✅ Firebase Admin SDK initialized');
  } catch (error) {
    console.error('❌ Firebase initialization error:', error);
  }
}

// Tap type to emoji and message mapping
const tapTypeConfig = {
  'HAPPY_MISS': { emoji: '😊', title: 'You were missed 😊', body: 'Someone is thinking of you with a smile' },
  'SAD_MISS': { emoji: '😢', title: 'You were missed 😢', body: 'Someone is missing you' },
  'NAUGHTY_MISS': { emoji: '😏', title: 'You were missed 😏', body: 'Someone is thinking of you' },
  'LOVING_MISS': { emoji: '❤️', title: 'You were missed ❤️', body: 'Someone loves and misses you' },
  'EXCITED_MISS': { emoji: '🤗', title: 'You were missed 🤗', body: 'Someone can\'t wait to see you' },
  'SLEEPY_MISS': { emoji: '😴', title: 'You were missed 😴', body: 'Someone is thinking of you before sleep' },
  'PLAYFUL_MISS': { emoji: '😄', title: 'You were missed 😄', body: 'Someone is playfully missing you' },
  'THINKING_MISS': { emoji: '🤔', title: 'You were missed 🤔', body: 'Someone is thinking about you' }
};

class PushService {
  async sendTapNotification(toFcmToken, pairId, tapType = 'LOVING_MISS', fromUserId = null, customEmoji = null, message = null) {
    if (!firebaseInitialized) {
      console.error('Firebase not initialized');
      return { success: false, error: 'Firebase not initialized' };
    }

    // Calculate personalized message to make recipient feel special
    const Tap = require('../models/Tap');
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    // Count how many taps recipient received today
    const tapsToday = await Tap.count({
      where: {
        pairId,
        toUserId: fromUserId ? (await require('../models/Pair').findByPk(pairId)).then(p => 
          p.user1Id === fromUserId ? p.user2Id : p.user1Id
        ) : null,
        timestamp: {
          [require('sequelize').Op.gte]: today
        }
      }
    });

    // Create personalized message
    let personalizedBody = message;
    if (!personalizedBody) {
      if (tapsToday === 0) {
        personalizedBody = "You're the first person they thought of today. You're special! ❤️";
      } else if (tapsToday === 1) {
        personalizedBody = "You're the only person they tapped today. You're special! ❤️";
      } else {
        personalizedBody = customEmoji ? 
          `Someone is thinking of you ${customEmoji}` : 
          (tapTypeConfig[tapType] || tapTypeConfig['LOVING_MISS']).body;
      }
    }

    // Use custom emoji if provided, otherwise use tap type config
    const config = customEmoji ? {
      emoji: customEmoji,
      title: `You were missed ${customEmoji}`,
      body: personalizedBody
    } : {
      ...(tapTypeConfig[tapType] || tapTypeConfig['LOVING_MISS']),
      body: personalizedBody
    };

    try {
      const message = {
        notification: {
          title: config.title,
          body: config.body
        },
        data: {
          type: 'tap',
          tapType: tapType,
          emoji: config.emoji,
          pairId: pairId,
          fromUserId: fromUserId || '',
          timestamp: new Date().toISOString()
        },
        token: toFcmToken,
        android: {
          priority: 'high',
          notification: {
            channelId: 'tap_channel',
            sound: 'default',
            vibrateTimingsMillis: this.getVibrationPattern(tapType)
          }
        },
        apns: {
          headers: {
            'apns-priority': '10'
          },
          payload: {
            aps: {
              sound: 'default',
              badge: 1
            }
          }
        }
      };

      const response = await admin.messaging().send(message);
      return { success: true, messageId: response };
    } catch (error) {
      console.error('FCM push notification error:', error);
      
      // Handle invalid token
      if (error.code === 'messaging/invalid-registration-token' || 
          error.code === 'messaging/registration-token-not-registered') {
        return { success: false, error: 'invalid_token', shouldRemove: true };
      }
      
      return { success: false, error: error.message };
    }
  }

  getVibrationPattern(tapType) {
    // Vibration patterns in milliseconds (delay, vibrate, delay, vibrate, ...)
    const patterns = {
      'HAPPY_MISS': [0, 200, 100, 200],
      'SAD_MISS': [0, 300, 200, 300],
      'NAUGHTY_MISS': [0, 100, 50, 100, 50, 100],
      'LOVING_MISS': [0, 250, 100, 250],
      'EXCITED_MISS': [0, 150, 80, 150, 80, 150],
      'SLEEPY_MISS': [0, 400],
      'PLAYFUL_MISS': [0, 100, 50, 100, 50, 100, 50, 100],
      'THINKING_MISS': [0, 200, 150, 200]
    };
    return patterns[tapType] || patterns['LOVING_MISS'];
  }

  async sendToMultipleTokens(tokens, pairId, tapType = 'LOVING_MISS', fromUserId = null, customEmoji = null) {
    if (!firebaseInitialized || tokens.length === 0) {
      return { success: false };
    }

    // Use custom emoji if provided, otherwise use tap type config
    const config = customEmoji ? {
      emoji: customEmoji,
      title: `You were missed ${customEmoji}`,
      body: 'Someone is thinking of you'
    } : (tapTypeConfig[tapType] || tapTypeConfig['LOVING_MISS']);

    try {
      const message = {
        notification: {
          title: config.title,
          body: config.body
        },
        data: {
          type: 'tap',
          tapType: tapType || '',
          emoji: config.emoji,
          customEmoji: customEmoji || '',
          message: message || '',
          pairId: pairId,
          fromUserId: fromUserId || '',
          timestamp: new Date().toISOString()
        },
        tokens: tokens,
        android: {
          priority: 'high',
          notification: {
            vibrateTimingsMillis: this.getVibrationPattern(tapType || 'LOVING_MISS')
          }
        }
      };

      const response = await admin.messaging().sendEachForMulticast(message);
      return { 
        success: true, 
        successCount: response.successCount,
        failureCount: response.failureCount
      };
    } catch (error) {
      console.error('FCM multicast error:', error);
      return { success: false, error: error.message };
    }
  }
}

module.exports = new PushService();
