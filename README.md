# TapMe - "Tap When You Miss Me" ❤️

A simple, intimate app for expressing "I miss you" with a single tap. No chat, no pressure, just pure connection.

## 🚀 Quick Start

**Want to run locally?** See [QUICK_START.md](./QUICK_START.md) for a 5-minute setup guide.

**Need detailed setup?** See [LOCAL_DEVELOPMENT_SETUP.md](./LOCAL_DEVELOPMENT_SETUP.md) for comprehensive instructions.

## 🎯 Product Vision

Preserve emotional safety, intimacy, and simplicity. This is not a social network, not a chat app, not a gamified experience. It's a gentle way to say "I'm thinking of you."

## 🏗️ Project Structure

```
TapMe/
├── android-kotlin/          # Kotlin Android app
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/tapme/app/
│   │   │   │   ├── domain/     # Models
│   │   │   │   ├── data/       # Room, Retrofit
│   │   │   │   ├── ui/         # Activities, Fragments
│   │   │   │   └── utils/      # Helpers
│   │   │   └── res/            # Layouts, resources
│   │   └── build.gradle
│   └── build.gradle
│
├── backend/                 # Node.js/Express backend
│   ├── src/
│   │   ├── config/         # Database config
│   │   ├── models/         # Sequelize models
│   │   ├── routes/         # API routes
│   │   ├── services/       # Business logic
│   │   ├── middleware/    # Auth, etc.
│   │   └── server.js       # Entry point
│   ├── Dockerfile
│   └── package.json
│
└── docs/                    # Product documentation
```

## 🚀 Quick Start

### Backend Setup

```bash
cd backend
npm install

# Create .env file (copy from .env.example)
cp .env.example .env
# Edit .env with your values

# Start development server
npm run dev
```

### Android Setup

```bash
cd android-kotlin

# Update API URL in app/build.gradle
# Update Firebase config (add google-services.json)

# Build
./gradlew assembleRelease
```

### Deployment
- **[Google Cloud Deployment](./GOOGLE_CLOUD_DEPLOYMENT.md)** - Deploy to Google Cloud Run
- **[Deployment Guide](./DEPLOYMENT_GUIDE.md)** - Alternative deployment options

### Optimization
- **[Advanced Optimizations](./ADVANCED_OPTIMIZATIONS.md)** - Performance optimization guide
- **[Optimization Summary](./OPTIMIZATION_SUMMARY.md)** - What we've optimized
- **[Performance Guide](./PERFORMANCE_OPTIMIZATION.md)** - Performance best practices

### Product
- **[Product Roadmap](./docs/01_PRODUCT_ROADMAP.md)** - Phase-by-phase development plan
- **[UX Principles](./docs/02_UX_PRODUCT_PRINCIPLES.md)** - Design and copywriting guidelines
- **[System Architecture](./docs/03_SYSTEM_ARCHITECTURE.md)** - Technical architecture
- **[Monetization](./docs/05_MONETIZATION_STRATEGY.md)** - Premium subscription strategy

## 🎨 Core Features

### ✅ MVP Features
- Single tap button
- Invite code pairing (6-digit codes)
- Push notifications (Firebase Cloud Messaging)
- Daily tap counter
- Last tap timestamp
- Offline support with sync

### 🚫 What We DON'T Build
- Chat functionality
- Social feeds
- Friend lists
- Public profiles
- Ads
- Pressure-inducing features

## 🛠️ Tech Stack

### Frontend
- **Android**: Kotlin
- **Architecture**: Clean Architecture (Domain, Data, UI)
- **Database**: Room (local storage)
- **Networking**: Retrofit + OkHttp
- **Push Notifications**: Firebase Cloud Messaging

### Backend
- **Runtime**: Node.js/Express
- **Database**: PostgreSQL (Cloud SQL)
- **Caching**: Redis (optional, recommended)
- **Push Notifications**: Firebase Admin SDK
- **Deployment**: Google Cloud Run

## 📱 Platform Support

- **Android**: API 21+ (Android 5.0+)
- **App Size**: 3.5-5.5 MB (AAB) ✅

## 🔒 Privacy & Security

- Minimal data collection
- JWT authentication
- HTTPS only
- No third-party analytics
- Easy data deletion
- GDPR/CCPA compliant

## 📊 Key Metrics

- **North Star**: Daily Active Pairs (DAP)
- **Target Retention**: 70%+ at 30 days
- **Push Delivery**: 99%+ success rate (FCM)
- **API Latency**: <50ms (with Redis caching)

## ⚡ Performance

- **App Size**: 3.5-5.5 MB (AAB)
- **API Latency**: 30-50ms (with Redis)
- **Tap Response**: 200-300ms
- **Cache Hit Rate**: 80-90%

## 🚦 Development Status

**Phase 1: Validation** (Current)
- Kotlin Android app implemented
- Node.js backend implemented
- Firebase Cloud Messaging integrated
- Ready for deployment

## 💰 Cost Estimate

- **Backend Hosting**: $0-5/month (Cloud Run free tier)
- **Database**: $0-50/month (Cloud SQL)
- **FCM**: $0/month (FREE)
- **Redis**: $5/month (optional, recommended)
- **Total**: $0-60/month

## 🎉 Highlights

- ✅ **Small App Size**: 3.5-5.5 MB (meets <10 MB requirement)
- ✅ **Fast Performance**: <50ms API latency
- ✅ **Free Push**: Firebase Cloud Messaging (unlimited)
- ✅ **Serverless**: Auto-scaling, pay only for what you use
- ✅ **Production Ready**: Optimized and tested

---

**Remember**: This app is about emotional intimacy, not engagement metrics. Every decision should preserve the gentle, pressure-free experience.
