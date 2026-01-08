# System Architecture

## Android App Architecture (Kotlin)

```
android-kotlin/app/src/main/java/com/tapme/app/
├── domain/
│   └── models/              # Data models (User, Pair, Tap, TapStats)
├── data/
│   ├── local/               # Room database
│   │   ├── TapDatabase.kt
│   │   ├── entity/
│   │   └── dao/
│   └── remote/              # Retrofit API
│       ├── ApiService.kt
│       └── RetrofitClient.kt
├── ui/
│   ├── tap/                 # Tap screen
│   ├── pairing/             # Pairing screen
│   └── stats/               # Stats screen
└── utils/                    # Helpers (DateFormatter, PreferencesManager)
```

## Architecture Pattern: Clean Architecture

**Why Clean Architecture?**
- Separation of concerns
- Testable
- Maintainable
- Scalable

**Layers**:
- **Domain**: Business logic and models
- **Data**: Repository pattern (Room + Retrofit)
- **UI**: Activities, Fragments, ViewModels

---

## Backend: Node.js/Express

### Services Used
- **PostgreSQL (Cloud SQL)**: Primary database
- **Firebase Cloud Messaging (FCM)**: Push notifications
- **JWT**: User authentication
- **Redis**: Caching (optional, recommended)
- **Google Cloud Run**: Serverless deployment

### Data Models

#### PostgreSQL Tables

**users**
```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  deviceId VARCHAR UNIQUE NOT NULL,
  fcmToken VARCHAR,
  pairId VARCHAR,
  premium BOOLEAN DEFAULT false,
  createdAt TIMESTAMP DEFAULT NOW(),
  updatedAt TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_users_deviceId ON users(deviceId);
CREATE INDEX idx_users_pairId ON users(pairId);
CREATE INDEX idx_users_fcmToken ON users(fcmToken);
```

**pairs**
```sql
CREATE TABLE pairs (
  id VARCHAR(6) PRIMARY KEY,
  user1Id UUID REFERENCES users(id),
  user2Id UUID REFERENCES users(id),
  lastTapAt TIMESTAMP,
  lastTapAtReverse TIMESTAMP,
  createdAt TIMESTAMP DEFAULT NOW(),
  updatedAt TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_pairs_user1Id ON pairs(user1Id);
CREATE INDEX idx_pairs_user2Id ON pairs(user2Id);
```

**taps**
```sql
CREATE TABLE taps (
  id UUID PRIMARY KEY,
  pairId VARCHAR(6) NOT NULL,
  fromUserId UUID NOT NULL,
  toUserId UUID NOT NULL,
  timestamp TIMESTAMP DEFAULT NOW(),
  synced BOOLEAN DEFAULT true
);

CREATE INDEX idx_taps_pair_user_time ON taps(pairId, fromUserId, timestamp DESC);
CREATE INDEX idx_taps_pair_time ON taps(pairId, timestamp DESC);
```

### Security

Security is handled at the API level:
- JWT authentication middleware
- Route-level authorization checks
- Input validation
- Rate limiting

---

## Push Notification Flow

### Architecture
1. **User A taps button** in Android app
2. **Android app calls REST API** `POST /api/taps/send`
3. **Backend validates request** (auth, rate limits)
4. **Backend saves tap** to PostgreSQL database
5. **Backend checks rate limits** (max 10 taps/hour per pair)
6. **Backend fetches recipient's FCM token** from `users` table
7. **Backend sends FCM** via Firebase Admin SDK
8. **Recipient receives push** notification
9. **App updates local state** when opened (foreground or background)

### Backend Implementation

```javascript
// In backend/src/routes/taps.js
router.post('/send', auth, async (req, res) => {
  const fromUserId = req.userId;
  const { pairId } = req.body;

  // Check rate limits
  const rateLimit = await rateLimitService.checkTapLimit(pairId, fromUserId);
  if (!rateLimit.allowed) {
    return res.status(429).json({ error: 'Rate limit exceeded' });
  }

  // Create tap
  const tap = await Tap.create({
    pairId,
    fromUserId,
    toUserId,
    timestamp: new Date()
  });

  // Get recipient's FCM token and send notification
  const recipient = await User.findByPk(toUserId);
  if (recipient && recipient.fcmToken) {
    pushService.sendTapNotification(recipient.fcmToken, pairId)
      .catch(err => console.error('Push failed:', err));
  }
});
    
    const recentTaps = await admin.firestore()
      .collection('taps')
      .where('pairId', '==', tap.pairId)
      .where('fromUserId', '==', tap.fromUserId)
      .where('timestamp', '>', oneHourAgo)
      .get();
    
    if (recentTaps.size >= 10) {
      console.log('Rate limit exceeded');
      return null;
    }
    
    // Get recipient's FCM token
    const userDoc = await admin.firestore()
      .collection('users')
      .doc(tap.toUserId)
      .get();
    
    const fcmToken = userDoc.data()?.fcmToken;
    if (!fcmToken) {
      console.log('No FCM token for user');
      return null;
    }
    
    // Send notification
    const message = {
      notification: {
        title: 'You were missed ❤️',
        body: 'Someone is thinking of you',
      },
      token: fcmToken,
      android: {
        priority: 'high',
      },
      apns: {
        headers: {
          'apns-priority': '10',
        },
      },
    };
    
    try {
      await admin.messaging().send(message);
      console.log('Notification sent successfully');
    } catch (error) {
      console.error('Error sending notification:', error);
    }
  });
```

---

## Offline Support

### Strategy
- **Local SQLite/Hive queue** for taps when offline
- **Background sync** when online
- **Conflict resolution**: Server timestamp wins
- **Retry logic**: Exponential backoff

### Implementation
- Use Hive for local storage (lightweight, fast)
- Queue taps in `offline_taps` box
- On app start, sync all queued taps
- On network reconnect, trigger sync

---

## Performance Considerations

### Firestore Queries
- **Index required queries**: Create composite indexes for `pairId + fromUserId + timestamp`
- **Limit query results**: Only fetch last 7 days for free tier
- **Use streams efficiently**: Cancel subscriptions when not needed

### Push Notifications
- **Batch sends**: Not needed for this use case (1:1)
- **Retry logic**: Cloud Functions handles retries
- **Fallback**: Local notifications if FCM fails

### App Performance
- **Lazy loading**: Load stats only when Stats tab is opened
- **Image optimization**: No images in MVP
- **State management**: Riverpod providers are efficient

---

## Scalability

### Current Limits (MVP)
- 1,000 active pairs
- 10,000 taps/day
- Firebase free tier sufficient

### Scale Considerations
- **Firestore**: Scales automatically, but costs increase
- **Cloud Functions**: Scales automatically
- **FCM**: Handles millions of messages

### Cost Optimization
- **Efficient queries**: Use indexes, limit results
- **Batch writes**: Not applicable (1 tap = 1 write)
- **Cache FCM tokens**: Store in Firestore, update on refresh

---

## Security

### Authentication
- **Anonymous auth**: For quick start
- **Email auth**: Optional, for account recovery
- **No password required**: Reduces friction

### Data Privacy
- **Encryption at rest**: Firestore handles this
- **Encryption in transit**: HTTPS/TLS
- **No third-party analytics**: Privacy-first
- **User data deletion**: Allow users to delete all data

### Rate Limiting
- **Client-side**: Basic checks in Flutter
- **Server-side**: Cloud Functions enforce limits
- **Per-pair limits**: 10 taps/hour, 50 taps/day

