# Local Development Setup Guide

This guide will help you set up and run the TapMe app locally for development and testing.

## 📋 Prerequisites

### Required Software
- **Node.js** (v16 or higher) - [Download](https://nodejs.org/)
- **PostgreSQL** (v12 or higher) - [Download](https://www.postgresql.org/download/)
- **Android Studio** - [Download](https://developer.android.com/studio)
- **Java JDK 11+** - Included with Android Studio
- **Firebase Account** - [Create](https://console.firebase.google.com/)
- **Git** - [Download](https://git-scm.com/)

### Optional (Recommended)
- **Postman** or **Insomnia** - For API testing
- **pgAdmin** or **DBeaver** - PostgreSQL GUI client

---

## 🗂️ Project Structure

```
TapMe/
├── backend/                    # Node.js backend
│   ├── src/
│   │   ├── config/            # Database & app config
│   │   ├── models/            # Sequelize models
│   │   ├── routes/            # API routes
│   │   ├── services/          # Business logic
│   │   ├── middleware/        # Auth & other middleware
│   │   └── server.js          # Entry point
│   ├── migrations/            # Database migrations
│   ├── package.json
│   ├── .env.example           # Environment variables template
│   └── Dockerfile
│
├── android-kotlin/             # Android app
│   ├── app/
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/      # Kotlin source code
│   │   │       ├── res/       # Resources (layouts, strings, etc.)
│   │   │       └── AndroidManifest.xml
│   │   └── build.gradle
│   ├── build.gradle
│   └── settings.gradle
│
├── docs/                       # Documentation
└── README.md
```

---

## 🚀 Step-by-Step Setup

### 1. Clone and Navigate

```bash
cd /Users/atulkarn/Desktop/TapMe
```

### 2. Backend Setup

#### 2.1 Install Dependencies

```bash
cd backend
npm install
```

#### 2.2 Set Up PostgreSQL Database

**Option A: Using PostgreSQL locally**

1. Install PostgreSQL (if not installed)
2. Create database:
```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE tapme_dev;
CREATE USER tapme_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE tapme_dev TO tapme_user;
\q
```

**Option B: Using Docker (Recommended)**

```bash
# Run PostgreSQL in Docker
docker run --name tapme-postgres \
  -e POSTGRES_DB=tapme_dev \
  -e POSTGRES_USER=tapme_user \
  -e POSTGRES_PASSWORD=your_password \
  -p 5432:5432 \
  -d postgres:14
```

#### 2.3 Configure Environment Variables

```bash
cd backend
cp .env.example .env
```

Edit `.env` file:

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tapme_dev
DB_USER=tapme_user
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_super_secret_jwt_key_change_this_in_production
JWT_EXPIRES_IN=30d

# Server
PORT=8080
NODE_ENV=development

# Firebase (for push notifications)
FIREBASE_SERVICE_ACCOUNT={"type":"service_account","project_id":"your-project-id",...}

# CORS (optional)
ALLOWED_ORIGINS=*

# Rate Limiting (optional)
RATE_LIMIT_WINDOW_MS=3600000
RATE_LIMIT_MAX_REQUESTS=100

# Redis (optional, for caching)
REDIS_URL=redis://localhost:6379
```

#### 2.4 Set Up Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use existing)
3. Go to Project Settings → Service Accounts
4. Click "Generate New Private Key"
5. Copy the JSON content to `.env` as `FIREBASE_SERVICE_ACCOUNT` (as JSON string)

**Note**: For local development, you can use Firebase Emulator or skip FCM temporarily.

#### 2.5 Run Database Migrations

```bash
# Option 1: Using Sequelize sync (development only)
npm run dev

# Option 2: Run SQL migrations manually
psql -U tapme_user -d tapme_dev -f migrations/add_user_code_and_whitelist.sql
psql -U tapme_user -d tapme_dev -f migrations/add_custom_emoji_to_taps.sql
psql -U tapme_user -d tapme_dev -f migrations/add_message_to_taps.sql
psql -U tapme_user -d tapme_dev -f migrations/add_streak_to_pairs.sql
```

#### 2.6 Start Backend Server

```bash
# Development mode (with auto-reload)
npm run dev

# Or production mode
npm start
```

**Expected output:**
```
✅ Database connected
✅ Database models synced
🚀 Server running on port 8080
📱 Environment: development
```

**Test the API:**
```bash
# Health check
curl http://localhost:8080/health

# Should return: {"status":"ok","timestamp":"..."}
```

---

### 3. Android App Setup

#### 3.1 Install Android Studio

1. Download and install [Android Studio](https://developer.android.com/studio)
2. Open Android Studio
3. Install Android SDK (API 24+ recommended)

#### 3.2 Set Up Firebase for Android

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Click "Add app" → Android
4. Register app:
   - Package name: `com.tapme.app`
   - App nickname: TapMe
5. Download `google-services.json`
6. Place it in: `android-kotlin/app/google-services.json`

#### 3.3 Configure Backend URL

Edit `android-kotlin/app/src/main/java/com/tapme/app/data/remote/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/api/" // Android Emulator
// OR
private const val BASE_URL = "http://YOUR_LOCAL_IP:8080/api/" // Physical device
```

**Find your local IP:**
```bash
# macOS/Linux
ifconfig | grep "inet " | grep -v 127.0.0.1

# Windows
ipconfig
```

#### 3.4 Open Project in Android Studio

1. Open Android Studio
2. File → Open → Select `android-kotlin` folder
3. Wait for Gradle sync to complete
4. Sync Project with Gradle Files (if needed)

#### 3.5 Build and Run

1. Connect Android device or start emulator
2. Click "Run" button (▶️) or press `Shift+F10`
3. Select device/emulator
4. App will install and launch

---

## 🧪 Testing

### Backend API Testing

**Using curl:**

```bash
# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"test-device-123","fcmToken":"test-token"}'

# Response: {"success":true,"token":"...","user":{...}}
```

**Using Postman:**

1. Import collection (create one with these endpoints):
   - `POST /api/auth/register`
   - `POST /api/pairs/create`
   - `POST /api/pairs/join`
   - `POST /api/taps/send`
   - `GET /api/taps/stats/:pairId`

2. Set base URL: `http://localhost:8080/api`

### Android App Testing

1. **Test Registration:**
   - Open app
   - Should auto-register with device ID
   - Check logs for API calls

2. **Test Pairing:**
   - Create pair (get invite code)
   - Join pair with code
   - Verify pair creation

3. **Test Tapping:**
   - Send tap
   - Check remaining taps count
   - Test cooldown period
   - Verify push notification (if FCM configured)

4. **Test Whitelist:**
   - Go to "Allowed" tab
   - Add user code
   - Verify tap validation

---

## 🔧 Troubleshooting

### Backend Issues

**Database Connection Error:**
```bash
# Check PostgreSQL is running
pg_isready

# Check connection
psql -U tapme_user -d tapme_dev -h localhost
```

**Port Already in Use:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

**Firebase Not Initialized:**
- Check `.env` file has `FIREBASE_SERVICE_ACCOUNT`
- Verify JSON is properly escaped
- For local dev, you can skip FCM (comment out push notification code)

### Android Issues

**Build Errors:**
```bash
# Clean and rebuild
cd android-kotlin
./gradlew clean
./gradlew build
```

**Network Error:**
- Check backend is running
- Verify BASE_URL in RetrofitClient
- For emulator: use `10.0.2.2` instead of `localhost`
- For physical device: use your computer's local IP

**Firebase Not Working:**
- Verify `google-services.json` is in correct location
- Check package name matches Firebase project
- Rebuild project after adding `google-services.json`

---

## 📝 Development Workflow

### 1. Start Backend
```bash
cd backend
npm run dev
```

### 2. Start Android App
- Open in Android Studio
- Run on device/emulator

### 3. Make Changes
- Backend: Auto-reloads (if using `npm run dev`)
- Android: Rebuild and run

### 4. Test Changes
- Test API endpoints
- Test app functionality
- Check logs for errors

---

## 🗄️ Database Management

### View Database
```bash
# Using psql
psql -U tapme_user -d tapme_dev

# List tables
\dt

# View users
SELECT * FROM users;

# View pairs
SELECT * FROM pairs;

# View taps
SELECT * FROM taps;
```

### Reset Database (Development Only)
```bash
# Drop and recreate
psql -U tapme_user -d postgres -c "DROP DATABASE tapme_dev;"
psql -U tapme_user -d postgres -c "CREATE DATABASE tapme_dev;"

# Run migrations again
npm run dev  # Will sync models
```

---

## 🔐 Environment Variables Reference

### Backend (.env)

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `tapme_dev` |
| `DB_USER` | Database user | `tapme_user` |
| `DB_PASSWORD` | Database password | `your_password` |
| `JWT_SECRET` | JWT signing secret | `your_secret_key` |
| `JWT_EXPIRES_IN` | Token expiration | `30d` |
| `PORT` | Server port | `8080` |
| `NODE_ENV` | Environment | `development` |
| `FIREBASE_SERVICE_ACCOUNT` | Firebase JSON (as string) | `{"type":"service_account",...}` |
| `REDIS_URL` | Redis connection (optional) | `redis://localhost:6379` |

---

## 📚 Next Steps

1. **Set up version control:**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   ```

2. **Create .gitignore:**
   - `.env` files
   - `node_modules/`
   - `build/` folders
   - `*.apk` files

3. **Set up CI/CD** (optional):
   - GitHub Actions
   - GitLab CI
   - CircleCI

4. **Deploy to production:**
   - See `DEPLOYMENT_GUIDE.md`
   - See `GOOGLE_CLOUD_DEPLOYMENT.md`

---

## ✅ Checklist

- [ ] Node.js installed
- [ ] PostgreSQL installed and running
- [ ] Backend dependencies installed
- [ ] Database created
- [ ] `.env` file configured
- [ ] Firebase project created
- [ ] Backend server running
- [ ] Android Studio installed
- [ ] `google-services.json` added
- [ ] Android app builds successfully
- [ ] App connects to backend
- [ ] Can register user
- [ ] Can create/join pair
- [ ] Can send taps
- [ ] Push notifications work (optional)

---

## 🆘 Need Help?

- Check logs: `backend` console and Android Studio Logcat
- Verify all prerequisites are installed
- Check network connectivity
- Review error messages carefully
- Check Firebase console for FCM issues

---

**Happy Coding! 🚀**
