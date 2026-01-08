# Push Notification Design

## Notification Payload

### Standard Payload
```json
{
  "notification": {
    "title": "You were missed ❤️",
    "body": "Someone is thinking of you"
  },
  "data": {
    "type": "tap",
    "pairId": "ABC123",
    "timestamp": "2024-01-15T10:30:00Z"
  },
  "android": {
    "priority": "high",
    "notification": {
      "channelId": "tap_channel",
      "sound": "default",
      "icon": "@mipmap/ic_launcher"
    }
  },
  "apns": {
    "headers": {
      "apns-priority": "10"
    },
    "payload": {
      "aps": {
        "sound": "default",
        "badge": 1
      }
    }
  }
}
```

---

## Copy Examples

### Primary Notification
**Title**: "You were missed ❤️"  
**Body**: "Someone is thinking of you"

**Why**: 
- Warm, emotional
- No pressure to respond
- Doesn't explicitly say "partner" (preserves privacy)

### Alternative Options (A/B Test)
1. **Title**: "A gentle reminder ❤️"  
   **Body**: "You're being thought of"

2. **Title**: "Thinking of you"  
   **Body**: "Someone misses you"

3. **Title**: "You were missed ❤️"  
   **Body**: "A tap from someone special"

---

## Rate Limiting

### Limits
- **Per pair**: 10 taps/hour maximum
- **Per pair**: 50 taps/day maximum
- **Per user**: No global limit (only pair-based)

### Implementation
1. **Client-side check**: Before sending tap, check today's count
2. **Server-side enforcement**: Cloud Function validates before sending notification
3. **Graceful degradation**: If limit reached, tap is saved but no notification sent

### User Experience
- **No error message**: Don't tell user they've hit limit (reduces pressure)
- **Silent failure**: Tap is recorded, but notification is throttled
- **Reset at midnight**: Daily limits reset

---

## Notification Timing

### Immediate Delivery
- **Default**: Send immediately when tap occurs
- **Rationale**: Emotional signals are time-sensitive

### Do Not Disturb (Future)
- **Optional setting**: Allow users to set quiet hours
- **Default**: No quiet hours (preserve simplicity)
- **Implementation**: Check user's timezone and quiet hours before sending

---

## Notification Channels (Android)

### Primary Channel: `tap_channel`
- **Name**: "Tap Notifications"
- **Description**: "Notifications when someone taps you"
- **Importance**: High
- **Sound**: Default
- **Vibration**: Default
- **Lights**: Default

### User Control
- Users can disable notifications in system settings
- App respects system-level notification preferences
- No in-app notification toggle (keep it simple)

---

## Badge Count (iOS)

### Strategy
- **Show unread tap count**: Number of taps received since last app open
- **Reset on app open**: Clear badge when user opens app
- **Max badge**: 99+ (don't show exact count if >99)

### Implementation
```dart
// Update badge when notification received
await FlutterLocalNotificationsPlugin()
    .show(0, title, body, details, payload: 'tap');

// Clear badge when app opens
await FlutterLocalNotificationsPlugin().cancelAll();
```

---

## Notification Actions (Future)

### Potential Actions (Phase 4+)
- **None in MVP**: Keep it simple
- **Future**: "Tap back" quick action (controversial - adds pressure)

### Decision: No Actions in MVP
- Preserves zero-obligation principle
- Keeps notification simple
- User can open app if they want to respond

---

## Delivery Reliability

### Retry Strategy
- **Cloud Functions**: Automatic retry (3 attempts)
- **Exponential backoff**: 1s, 2s, 4s delays
- **Failure handling**: Log failures, don't notify user

### Fallback
- **Local notification**: If FCM fails, show local notification
- **Background sync**: Retry when app comes to foreground

### Monitoring
- **Delivery rate**: Track % of successful deliveries
- **Failure reasons**: Log FCM errors
- **Target**: 95%+ delivery rate

---

## Privacy Considerations

### Notification Content
- **No personal info**: Don't include names or relationship details
- **Generic copy**: "Someone" not "Your partner"
- **No context**: Don't reveal when/how many times

### Data in Payload
- **Minimal data**: Only `pairId` and `timestamp`
- **No user IDs**: Don't expose user information
- **Encrypted**: FCM handles encryption in transit

---

## Testing

### Test Cases
1. **Single tap**: Verify notification arrives
2. **Rate limit**: Verify 11th tap in hour doesn't send notification
3. **Offline tap**: Verify notification sent when recipient comes online
4. **Multiple pairs**: Verify correct recipient receives notification
5. **App in foreground**: Verify local notification shown
6. **App in background**: Verify push notification received
7. **App closed**: Verify push notification received

### Test Devices
- Android (multiple versions)
- iOS (multiple versions)
- Different network conditions (WiFi, cellular, offline)

