# UX & Product Principles

## Core Principles

### 1. Zero Obligation
- **No read receipts**: Recipient never knows if you saw their tap
- **No response required**: Taps are one-way signals, not conversations
- **No streaks that create pressure**: Gentle counters only, no "you'll break your streak" messages
- **No "you haven't tapped in X days" reminders**: Never guilt users

### 2. Emotional Safety
- **No public profiles**: Everything is private
- **Data is encrypted**: End-to-end where possible
- **User can unpair without notification**: Silent exit option
- **No "online/offline" status**: Preserves mystery and reduces pressure

### 3. Simplicity
- **One primary action**: Tap button
- **No settings unless necessary**: Defaults should work for 95% of users
- **No onboarding tutorial**: App should be intuitive

### 4. Intimacy Preservation
- **No chat**: Removes pressure to respond
- **No public sharing**: Keeps relationship private
- **No gamification beyond gentle streaks**: Avoid turning love into a game

---

## Copywriting Tone

### Notifications
**✅ Good**:
- "You were missed ❤️"
- "Someone is thinking of you"
- "A gentle reminder you're loved"

**❌ Bad**:
- "You have a new tap!" (too transactional)
- "Your partner tapped you!" (too explicit if relationship status unknown)
- "Tap back now!" (creates obligation)

### In-App Copy
**✅ Good**:
- "Last tap: 2 hours ago" (neutral, informative)
- "Today: 3 taps" (simple fact)
- "Tap when you miss me ❤️" (warm, inviting)

**❌ Bad**:
- "You haven't been tapped in 2 hours" (creates guilt)
- "Your partner is waiting!" (creates pressure)
- "Tap now to keep the streak!" (gamification pressure)

---

## What Breaks Trust

1. **Pushy notifications**: "You haven't opened the app in 3 days"
2. **Public data exposure**: Any sharing without explicit consent
3. **Pressure to respond**: Any UI that suggests obligation
4. **Ads or paywalls on core features**: Monetization must be optional

---

## What Builds Habit

1. **Reliable delivery**: 95%+ push notification success rate
2. **Gentle, positive reinforcement**: "You've tapped 5 times today" (not "You need to tap more")
3. **No guilt or obligation**: Users should feel free to ignore taps
4. **Consistent experience**: App works the same way every time

---

## Design Guidelines

### Colors
- **Primary**: Red (#FF5252) - Love, warmth
- **Background**: White - Clean, minimal
- **Text**: Dark grey (#424242) - Readable, not harsh

### Typography
- **Headings**: Roboto Bold, 20-24px
- **Body**: Roboto Regular, 16px
- **Buttons**: Roboto Medium, 16px

### Spacing
- **Padding**: 24px standard
- **Button height**: 56px minimum
- **Tap button**: 200x200px circle

### Animations
- **Tap button**: Gentle scale on press (0.95x)
- **Transitions**: 200ms ease-in-out
- **No aggressive animations**: Keep it calm

---

## User Flows

### First Time User
1. Open app
2. See empty state: "Not paired yet"
3. Navigate to Pairing tab
4. Generate or enter invite code
5. Return to Tap tab
6. See tap button

### Daily Use
1. Open app
2. See tap button and stats
3. Tap when feeling missing
4. Receive push notification (if recipient)
5. Check stats occasionally

### Pairing Flow
1. User A generates code
2. User A shares code (copy/paste)
3. User B enters code
4. Pair is created
5. Both users can now tap

---

## Error States

### No Internet
- Show offline indicator
- Queue taps locally
- Sync when online
- Don't block user from tapping

### Pairing Failed
- Clear error message: "Code not found or already used"
- Suggest generating new code
- Don't blame the user

### Push Notification Failed
- Retry automatically
- Don't notify user of failure
- Log for debugging

