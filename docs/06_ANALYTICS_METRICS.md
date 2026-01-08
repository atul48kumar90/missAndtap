# Analytics & Metrics

## North Star Metric

### Primary: **Daily Active Pairs (DAP)**
**Definition**: Number of unique pairs where at least one user opened the app in the last 24 hours.

**Why this metric?**
- Measures core engagement (pairs, not individuals)
- Aligns with product goal (connecting pairs)
- Less susceptible to single-user churn

**Target**: 
- Week 1: 50 DAP
- Month 1: 200 DAP
- Month 3: 1,000 DAP

---

## Key Metrics

### Engagement Metrics

#### Daily Active Users (DAU)
- **Definition**: Unique users who opened app in last 24 hours
- **Target**: 2x DAP (both users in pair)
- **Tracking**: Firebase Analytics

#### Taps Per Day
- **Definition**: Total taps sent across all pairs in 24 hours
- **Target**: 2-3 taps per pair per day (average)
- **Tracking**: Firestore aggregation

#### Tap Frequency
- **Definition**: Average time between taps in a pair
- **Target**: 8-12 hours (healthy rhythm)
- **Tracking**: Calculate from tap timestamps

#### Session Duration
- **Definition**: Average time user spends in app per session
- **Target**: <30 seconds (quick tap, minimal friction)
- **Tracking**: Firebase Analytics

### Retention Metrics

#### Day 1 Retention
- **Definition**: % of new users who return on day 2
- **Target**: 60%+
- **Tracking**: Firebase Analytics cohort analysis

#### Day 7 Retention
- **Definition**: % of new users who return on day 7
- **Target**: 40%+
- **Tracking**: Firebase Analytics cohort analysis

#### Day 30 Retention
- **Definition**: % of new users who return on day 30
- **Target**: 25%+
- **Tracking**: Firebase Analytics cohort analysis

#### Pair Retention
- **Definition**: % of pairs still active after 30 days
- **Target**: 70%+
- **Tracking**: Custom Firestore query

### Quality Metrics

#### Push Delivery Rate
- **Definition**: % of taps that successfully trigger push notification
- **Target**: 95%+
- **Tracking**: Cloud Functions logs

#### Offline Sync Success Rate
- **Definition**: % of queued taps that successfully sync
- **Target**: 98%+
- **Tracking**: Offline queue service logs

#### Error Rate
- **Definition**: % of taps that fail (network, rate limit, etc.)
- **Target**: <2%
- **Tracking**: Error logging in app

### Business Metrics (Phase 3+)

#### Premium Conversion Rate
- **Definition**: % of users who subscribe to premium
- **Target**: 5-10%
- **Tracking**: In-app purchase receipts

#### Monthly Recurring Revenue (MRR)
- **Definition**: Total monthly subscription revenue
- **Target**: $500+ by month 3
- **Tracking**: Payment platform APIs

#### Churn Rate
- **Definition**: % of premium users who cancel per month
- **Target**: <5%
- **Tracking**: Subscription status in Firestore

---

## Events to Track

### User Events

#### `app_opened`
- **When**: User opens app
- **Properties**: 
  - `source` (notification, direct, etc.)
  - `time_since_last_open` (hours)

#### `pair_created`
- **When**: User successfully pairs with someone
- **Properties**:
  - `method` (generated_code, entered_code)
  - `time_to_pair` (seconds from app install)

#### `tap_sent`
- **When**: User taps button
- **Properties**:
  - `pair_id` (hashed)
  - `taps_today` (count)
  - `time_since_last_tap` (hours)

#### `tap_received`
- **When**: User receives tap (opens app after notification)
- **Properties**:
  - `time_since_tap` (minutes)
  - `notification_opened` (boolean)

#### `stats_viewed`
- **When**: User opens Stats tab
- **Properties**: None

#### `pairing_viewed`
- **When**: User opens Pairing tab
- **Properties**: None

### Error Events

#### `tap_failed`
- **When**: Tap fails to send
- **Properties**:
  - `error_type` (network, rate_limit, etc.)
  - `retry_count`

#### `pairing_failed`
- **When**: Pairing attempt fails
- **Properties**:
  - `error_type` (invalid_code, already_paired, etc.)

#### `notification_failed`
- **When**: Push notification fails to deliver
- **Properties**:
  - `error_type` (fcm_error, no_token, etc.)

### Premium Events (Phase 3+)

#### `premium_viewed`
- **When**: User views premium options
- **Properties**: None

#### `premium_purchased`
- **When**: User subscribes to premium
- **Properties**:
  - `price` ($4.99)
  - `days_since_install`

#### `premium_cancelled`
- **When**: User cancels premium
- **Properties**:
  - `days_as_premium`
  - `cancellation_reason` (if available)

---

## What Metrics Indicate Emotional Harm

### Red Flags

#### High Uninstall Rate (>10%)
- **Indication**: Users feel pressured, annoyed, or violated
- **Action**: Review notifications, check for bugs, survey users

#### Low Tap Frequency (<1 tap per day)
- **Indication**: Users don't feel comfortable tapping
- **Action**: Review UX, check for friction, simplify flow

#### High Notification Opt-Out Rate (>20%)
- **Indication**: Notifications are too frequent or annoying
- **Action**: Review notification copy, reduce frequency, add quiet hours

#### Short Session Duration (<5 seconds)
- **Indication**: Users open app but don't engage
- **Action**: Check for errors, review onboarding, simplify UI

#### High Pair Churn (>50% pairs break within 30 days)
- **Indication**: Pairing is broken or users don't find value
- **Action**: Review pairing flow, check for technical issues

### Positive Indicators

#### Increasing Tap Frequency Over Time
- **Indication**: Users are building habit, feeling comfortable
- **Action**: Continue current approach

#### Long Session Duration (>20 seconds)
- **Indication**: Users are exploring, engaged
- **Action**: Consider adding gentle features (stats, history)

#### High Retention (>70% at 30 days)
- **Indication**: Users find value, app is working
- **Action**: Focus on growth, not feature changes

---

## Privacy-First Analytics

### What We Track
- ✅ Aggregated usage data
- ✅ Anonymous events
- ✅ Technical metrics (errors, performance)

### What We DON'T Track
- ❌ Personal information (names, emails)
- ❌ Relationship details
- ❌ Content of any kind (no content exists)
- ❌ Location data
- ❌ Device identifiers (beyond basic device type)

### Data Retention
- **Analytics data**: 26 months (Firebase default)
- **User data**: Deleted when user deletes account
- **Aggregated metrics**: Kept indefinitely (no PII)

### Compliance
- **GDPR**: Allow users to export/delete data
- **CCPA**: Respect opt-out requests
- **COPPA**: Not applicable (18+ app)

---

## Dashboard Design

### Key Metrics Dashboard (Internal)
1. **DAP** (large, prominent)
2. **DAU** (secondary)
3. **Taps Today** (real-time)
4. **Push Delivery Rate** (health metric)
5. **Retention Chart** (7-day, 30-day)

### User-Facing Stats (In-App)
- **Today's taps**: Simple count
- **Last tap time**: Relative time ("2 hours ago")
- **No comparisons**: Don't show "you tapped less than yesterday"
- **No pressure**: Stats are informational, not motivational

---

## A/B Testing Framework

### What to Test
1. **Notification copy**: Different emotional tones
2. **Tap button design**: Size, color, animation
3. **Onboarding**: Tutorial vs. no tutorial
4. **Premium pricing**: $3.99 vs. $4.99 vs. $6.99

### How to Test
- **Firebase Remote Config**: Feature flags
- **Firebase A/B Testing**: Built-in framework
- **Sample size**: 1,000+ users per variant
- **Duration**: 2 weeks minimum

### Decision Criteria
- **Primary metric**: DAP increase
- **Secondary**: Retention, engagement
- **Guardrail**: No increase in uninstall rate

