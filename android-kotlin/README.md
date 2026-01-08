# TapMe Android App

Native Android app built with Kotlin.

## Quick Start

1. **Open in Android Studio**
   - File → Open → Select `android-kotlin` folder

2. **Add Firebase Configuration**
   - Download `google-services.json` from Firebase Console
   - Place in `app/google-services.json`

3. **Configure Backend URL**
   - Edit `app/build.gradle` → `buildConfigField "BASE_URL"`
   - For emulator: `http://10.0.2.2:8080/api/`
   - For physical device: `http://YOUR_LOCAL_IP:8080/api/`

4. **Run**
   - Click Run ▶️ or press `Shift+F10`
   - Select device/emulator

## Project Structure

```
android-kotlin/
├── app/
│   ├── src/main/
│   │   ├── java/com/tapme/app/
│   │   │   ├── data/          # Data layer (API, database)
│   │   │   ├── domain/        # Domain models
│   │   │   ├── ui/            # UI (Fragments, Activities)
│   │   │   └── utils/         # Utilities
│   │   ├── res/               # Resources (layouts, strings, etc.)
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

## Features

- ✅ Multiple tap types with emojis
- ✅ Custom emoji support
- ✅ Tap Moments (optional messages)
- ✅ Tap Streaks tracking
- ✅ Full-screen incoming tap notifications
- ✅ User code whitelist management
- ✅ Daily tap limits & cooldown
- ✅ Celebration animations

## Build Variants

### Debug
```bash
./gradlew assembleDebug
```

### Release
```bash
./gradlew assembleRelease
```

### App Bundle (for Play Store)
```bash
./gradlew bundleRelease
```

## Configuration

### Backend URL

Edit `app/build.gradle`:

```gradle
buildConfigField "String", "BASE_URL", "\"http://10.0.2.2:8080/api/\""
```

### Firebase

1. Add `google-services.json` to `app/` folder
2. Rebuild project

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

## Dependencies

- Kotlin
- AndroidX (AppCompat, Material Design)
- Room Database
- Retrofit + OkHttp
- Firebase Cloud Messaging
- Navigation Component
- Lifecycle Components

## App Size

- **Debug APK**: ~8-12 MB
- **Release APK**: ~5-7 MB
- **App Bundle**: ~4-6 MB ✅

## Permissions

- `INTERNET` - API calls
- `ACCESS_NETWORK_STATE` - Network status
- `POST_NOTIFICATIONS` - Push notifications
- `VIBRATE` - Haptic feedback
- `SYSTEM_ALERT_WINDOW` - Full-screen notifications

## Troubleshooting

**Build errors?**
```bash
./gradlew clean
./gradlew build
```

**Network errors?**
- Check backend is running
- Verify BASE_URL is correct
- For emulator: use `10.0.2.2`
- For device: use computer's local IP

**Firebase not working?**
- Verify `google-services.json` exists
- Check package name matches Firebase project
- Rebuild after adding `google-services.json`
