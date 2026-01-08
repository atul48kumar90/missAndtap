# Deployment Guide: Kotlin + Lightweight Backend

## 🚀 Primary Deployment: Google Cloud

**See [GOOGLE_CLOUD_DEPLOYMENT.md](GOOGLE_CLOUD_DEPLOYMENT.md) for complete Google Cloud deployment guide.**

This is the recommended deployment option because:
- ✅ Free Firebase Cloud Messaging (FCM)
- ✅ Serverless auto-scaling
- ✅ Low latency globally
- ✅ Integrated ecosystem

## 🌍 Alternative Deployment Options

### Railway (Alternative)

If you prefer Railway over Google Cloud:

1. **Create Railway Account**
   - Go to [railway.app](https://railway.app)
   - Sign up with GitHub

2. **Deploy Backend**
   ```bash
   cd backend
   # Install Railway CLI
   npm i -g @railway/cli
   
   # Login
   railway login
   
   # Initialize project
   railway init
   
   # Add PostgreSQL
   railway add postgresql
   
   # Set environment variables
   railway variables set JWT_SECRET=your-secret-key
   railway variables set FIREBASE_SERVICE_ACCOUNT='{"type":"service_account",...}'
   railway variables set NODE_ENV=production
   
   # Deploy
   railway up
   ```

3. **Get Backend URL**
   - Railway provides a URL like: `https://your-app.railway.app`
   - Update Android app's `API_BASE_URL` in `build.gradle`

### Firebase Cloud Messaging Setup

1. **Create Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create new project
   - Enable Cloud Messaging

2. **Get Service Account Key**
   - Project Settings > Service Accounts
   - Generate new private key
   - Save JSON file securely

3. **Configure Android**
   - Add Android app in Firebase Console
   - Download `google-services.json`
   - Place in `android-kotlin/app/`
   - FCM is already configured in the app

### Database Setup

Railway automatically provisions PostgreSQL. The app will create tables on first run.

For manual setup:
```sql
-- Tables are auto-created by Sequelize
-- But you can verify with:
SELECT * FROM users;
SELECT * FROM pairs;
SELECT * FROM taps;
```

## 📱 Android App Configuration

### 1. Update API Base URL

Edit `android-kotlin/app/build.gradle`:
```gradle
buildConfigField "String", "API_BASE_URL", "\"https://your-app.railway.app/api/\""
```

### 2. Configure Firebase

1. Add Android app in Firebase Console
2. Download `google-services.json`
3. Place in `android-kotlin/app/`
4. FCM is already configured in the app

### 3. Build and Deploy

```bash
cd android-kotlin
./gradlew assembleRelease
# APK will be in app/build/outputs/apk/release/
```

## 🌍 Alternative Deployment Options

### Backend: Render

1. Create account at [render.com](https://render.com)
2. New Web Service → Connect GitHub repo
3. Set build command: `npm install`
4. Set start command: `node src/server.js`
5. Add PostgreSQL database
6. Set environment variables

### Backend: Fly.io

```bash
# Install flyctl
curl -L https://fly.io/install.sh | sh

# Login
fly auth login

# Launch app
cd backend
fly launch

# Add PostgreSQL
fly postgres create

# Attach database
fly postgres attach <db-name>
```

### Backend: DigitalOcean App Platform

1. Create account at [digitalocean.com](https://digitalocean.com)
2. Create App → Source: GitHub
3. Select backend directory
4. Add PostgreSQL component
5. Set environment variables

## ⚡ Performance Optimizations

### 1. Enable CDN (Cloudflare)

1. Add your Railway/Render domain to Cloudflare
2. Enable caching for static assets
3. Enable compression

### 2. Database Connection Pooling

Already configured in `database.js`:
- Max connections: 20
- Idle timeout: 10s
- Acquire timeout: 30s

### 3. Response Compression

Already enabled in `server.js`:
```javascript
app.use(compression());
```

### 4. Caching (Optional but Recommended)

Add Redis for caching:
```bash
# Railway
railway add redis

# Update .env
REDIS_URL=redis://...
REDIS_ENABLED=true
```

### 5. Android App Optimizations

- ✅ ProGuard enabled
- ✅ Resource shrinking
- ✅ Code minification
- ✅ HTTP connection pooling (OkHttp)
- ✅ Request timeouts configured

## 🔒 Security Checklist

### Backend
- [x] Helmet.js for security headers
- [x] CORS configured
- [x] Rate limiting enabled
- [x] JWT authentication
- [x] Environment variables for secrets
- [ ] HTTPS only (Railway/Render provide automatically)
- [ ] Input validation (add if needed)

### Android
- [x] ProGuard obfuscation
- [x] No hardcoded secrets
- [x] HTTPS only (enforce in Retrofit)
- [ ] Certificate pinning (optional)

## 📊 Monitoring

### Backend Logs

**Railway:**
```bash
railway logs
```

**Render:**
- View in dashboard

### Health Check

Endpoint: `GET /health`
- Returns: `{ status: 'ok', timestamp: '...' }`

### Error Tracking

Consider adding:
- Sentry for error tracking
- LogRocket for session replay
- Or use Winston logs (already included)

## 💰 Cost Estimates

### Google Cloud (Recommended) ✅
- **Cloud Run**: $0-5/month (2M free requests)
- **Cloud SQL**: $0-50/month (free tier available)
- **FCM**: $0/month (FREE)
- **Redis**: $5/month (optional)
- **Total**: $0-60/month

### Railway (Alternative)
- **Backend**: $5/month (Hobby plan)
- **PostgreSQL**: $5/month (Starter plan)
- **FCM**: $0/month (FREE)
- **Total**: ~$10/month

### Render (Alternative)
- **Backend**: Free tier (with limits) or $7/month
- **PostgreSQL**: Free tier or $7/month
- **FCM**: $0/month (FREE)
- **Total**: Free (limited) or ~$14/month

## 🚨 Troubleshooting

### Backend Won't Start
1. Check environment variables
2. Verify database connection
3. Check logs: `railway logs`

### Push Notifications Not Working
1. Verify Firebase service account JSON is correct
2. Check FCM token is saved in database
3. Verify Firebase project ID matches
4. Check Android app is registered in Firebase

### High Latency
1. Check database connection pool
2. Enable Redis caching
3. Use CDN (Cloudflare)
4. Check server region (choose closest to users)

### Android App Crashes
1. Check API base URL is correct
2. Verify Firebase setup (google-services.json)
3. Check ProGuard rules
4. Review logs: `adb logcat`

## 📈 Scaling

### When to Scale

**Backend:**
- >1000 concurrent users
- Response time >500ms
- Database connections maxed

**Database:**
- >10GB data
- Slow queries
- Connection pool exhausted

### Scaling Steps

1. **Vertical Scaling**: Upgrade Railway/Render plan
2. **Horizontal Scaling**: Add more instances
3. **Database**: Upgrade PostgreSQL plan
4. **Caching**: Add Redis
5. **CDN**: Use Cloudflare

## ✅ Production Checklist

- [ ] Environment variables set
- [ ] Database migrations run
- [ ] HTTPS enabled
- [ ] CORS configured correctly
- [ ] Rate limiting configured
- [ ] Error logging set up
- [ ] Health check endpoint working
- [ ] Android app API URL updated
- [ ] Firebase configured
- [ ] ProGuard rules tested
- [ ] Performance tested
- [ ] Security audit done

---

**Recommended Setup:**
- **Backend**: Google Cloud Run (serverless, auto-scaling)
- **Database**: Cloud SQL PostgreSQL (managed)
- **Push**: Firebase Cloud Messaging (FREE)
- **CDN**: Cloud CDN (optional, improves latency)
- **Caching**: Cloud Memorystore Redis (optional, recommended)
- **Monitoring**: Cloud Logging + health checks

**Expected Latency:**
- API Response: <50ms (with Redis caching)
- Push Notification: <1s (FCM)
- App Size: 3.5-5.5 MB ✅
