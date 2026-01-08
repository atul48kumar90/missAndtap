# Product Roadmap: Tap When You Miss Me

## Phase 1: Validation (Weeks 1-4)
**Goal**: Validate core emotional need and pairing mechanism

### Build
- ✅ Single-tap button UI
- ✅ Invite code pairing (6-digit codes)
- ✅ Basic push notifications
- ✅ Simple tap counter
- ✅ Onboarding flow

### Success Metrics
- 50+ paired couples
- 70%+ daily active users
- <5% uninstall rate
- Average 2-3 taps per day per user

### Risks
- **Pairing friction**: Mitigate with shareable links and clear instructions
- **Notification fatigue**: Implement rate limiting (10 taps/hour max)
- **Low engagement**: Validate emotional need before building

### What NOT to Build
- ❌ Chat functionality
- ❌ Social feed
- ❌ Friend lists
- ❌ Public profiles
- ❌ Ads
- ❌ Analytics dashboard for users

---

## Phase 2: MVP (Weeks 5-12)
**Goal**: Stable, reliable core experience

### Build
- ✅ Offline queue with sync
- ✅ Push notification reliability (retry logic)
- ✅ Last tap timestamp display
- ✅ Daily tap streak counter (gentle, no pressure)
- ✅ Basic error handling
- ✅ Privacy-first data model

### Success Metrics
- 95%+ push delivery rate
- <2% failed tap syncs
- 80%+ retention at 30 days
- Zero privacy incidents

### Risks
- **Firebase costs scaling**: Monitor usage, implement efficient queries
- **Platform push reliability**: Add fallback mechanisms, retry logic

---

## Phase 3: Monetization (Weeks 13-20)
**Goal**: Sustainable revenue without harming intimacy

### Build
- ✅ Premium subscription (optional)
- ✅ Premium features:
  - Custom tap sounds
  - Themes
  - Extended tap history (30+ days)
- ✅ Free tier: Core functionality unlimited
- ✅ No ads, no paywalls on core features

### Success Metrics
- 5-10% conversion to premium
- $4.99/month pricing
- Zero churn due to monetization
- Premium users show higher engagement

### What NEVER to Monetize
- ❌ Core tap functionality
- ❌ Push notifications
- ❌ Pairing
- ❌ Basic tap history (last 7 days)

---

## Phase 4: Scale (Weeks 21+)
**Goal**: Growth while preserving intimacy

### Build
- Referral system (invite friends to try with their partners)
- Optional anonymous usage insights (opt-in)
- Performance optimization
- Internationalization

### Success Metrics
- 10,000+ active pairs
- <1% support tickets
- 85%+ retention at 90 days

### Risks
- **Feature creep**: Strict product council, say no to 90% of requests
- **Community pressure**: Resist social features, maintain intimacy

---

## Decision Framework

### Green Light (Build)
- Improves reliability
- Enhances privacy
- Reduces friction
- Preserves emotional simplicity

### Red Light (Don't Build)
- Adds obligation or pressure
- Creates social dynamics
- Monetizes core features
- Complicates the experience

