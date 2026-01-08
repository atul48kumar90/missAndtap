# TapMe Backend

Node.js + Express backend for TapMe app.

## Quick Start

```bash
# Install dependencies
npm install

# Set up environment
cp .env.example .env
# Edit .env with your database credentials

# Start development server
npm run dev
```

## Environment Variables

See `.env.example` for all required variables.

**Required:**
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `JWT_SECRET`
- `FIREBASE_SERVICE_ACCOUNT` (for push notifications)

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register/Login user

### Pairs
- `POST /api/pairs/create` - Create pair (get invite code)
- `POST /api/pairs/join` - Join pair with invite code
- `GET /api/pairs/:pairId` - Get pair info

### Taps
- `POST /api/taps/send` - Send tap
- `GET /api/taps/stats/:pairId` - Get tap statistics

### Whitelist
- `GET /api/whitelist/my-code` - Get my user code
- `POST /api/whitelist/regenerate-code` - Regenerate user code
- `GET /api/whitelist/allowed-tappers` - Get allowed tappers list
- `POST /api/whitelist/add-tapper` - Add user to whitelist
- `DELETE /api/whitelist/remove-tapper/:tapperId` - Remove from whitelist

## Database Migrations

Run migrations manually:

```bash
psql -U tapme_user -d tapme_dev -f migrations/add_user_code_and_whitelist.sql
psql -U tapme_user -d tapme_dev -f migrations/add_custom_emoji_to_taps.sql
psql -U tapme_user -d tapme_dev -f migrations/add_message_to_taps.sql
psql -U tapme_user -d tapme_dev -f migrations/add_streak_to_pairs.sql
```

## Development

```bash
# Development mode (auto-reload)
npm run dev

# Production mode
npm start
```

## Testing

```bash
# Health check
curl http://localhost:8080/health

# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"test-device-123"}'
```

## Project Structure

```
backend/
├── src/
│   ├── config/          # Database & app config
│   ├── models/          # Sequelize models
│   ├── routes/           # API routes
│   ├── services/         # Business logic
│   ├── middleware/       # Auth middleware
│   └── server.js        # Entry point
├── migrations/           # SQL migrations
└── package.json
```
