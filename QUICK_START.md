# Quick Start Guide

Get the app running locally in 5 minutes!

## Prerequisites Check

```bash
# Check Node.js
node --version  # Should be v16+

# Check PostgreSQL
psql --version  # Should be installed

# Check Java
java -version  # Should be JDK 11+
```

## 1. Backend Setup (2 minutes)

```bash
# Navigate to backend
cd backend

# Install dependencies
npm install

# Copy environment template
cp .env.example .env

# Edit .env with your database credentials
# (Use any text editor)

# Start PostgreSQL (if using Docker)
docker run --name tapme-postgres \
  -e POSTGRES_DB=tapme_dev \
  -e POSTGRES_USER=tapme_user \
  -e POSTGRES_PASSWORD=password123 \
  -p 5432:5432 \
  -d postgres:14

# Start backend server
npm run dev
```

**Expected:** `🚀 Server running on port 8080`

## 2. Android Setup (3 minutes)

1. **Open Android Studio**
2. **File → Open** → Select `android-kotlin` folder
3. **Wait for Gradle sync**
4. **Add Firebase** (optional for local testing):
   - Download `google-services.json` from Firebase Console
   - Place in `android-kotlin/app/google-services.json`
5. **Run** ▶️ (or `Shift+F10`)

## 3. Test It!

1. **Backend:** Open http://localhost:8080/health
2. **Android:** App should register automatically
3. **Create Pair:** Get invite code
4. **Join Pair:** Use code on another device/emulator
5. **Send Tap:** Tap someone!

## Troubleshooting

**Backend won't start?**
- Check PostgreSQL is running: `pg_isready`
- Check `.env` file exists and has correct DB credentials

**Android can't connect?**
- Check backend is running: `curl http://localhost:8080/health`
- For emulator: BASE_URL should be `http://10.0.2.2:8080/api/`
- For physical device: Use your computer's IP address

**Need more help?**
- See `LOCAL_DEVELOPMENT_SETUP.md` for detailed guide

---

**That's it! You're ready to develop! 🚀**
