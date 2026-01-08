# Google Cloud Deployment Guide

## 🎯 Overview

This guide will help you deploy the Tap Me backend to Google Cloud using:
- **Cloud Run** - Serverless container deployment (scales automatically)
- **Cloud SQL** - Managed PostgreSQL database
- **Firebase Cloud Messaging (FCM)** - Push notifications (free, unlimited)
- **Cloud CDN** - Content delivery network (optional)

## ✅ Benefits of Google Cloud

- **FCM**: Free, unlimited push notifications
- **Cloud Run**: Pay only for what you use (scales to zero)
- **Cloud SQL**: Managed, automatic backups
- **Low Latency**: Global edge network
- **Integrated**: All services work together seamlessly

## 📋 Prerequisites

1. Google Cloud account ([cloud.google.com](https://cloud.google.com))
2. Google Cloud CLI installed
3. Firebase project created
4. Billing enabled (free tier available)

## 🚀 Step 1: Set Up Google Cloud Project

### 1.1 Create Project

```bash
# Install Google Cloud CLI (if not installed)
# macOS: brew install google-cloud-sdk
# Or download from: https://cloud.google.com/sdk/docs/install

# Login
gcloud auth login

# Create project
gcloud projects create tapme-backend --name="Tap Me Backend"

# Set as current project
gcloud config set project tapme-backend

# Enable billing (required for Cloud Run)
# Do this in Cloud Console: https://console.cloud.google.com
```

### 1.2 Enable Required APIs

```bash
# Enable Cloud Run API
gcloud services enable run.googleapis.com

# Enable Cloud SQL API
gcloud services enable sqladmin.googleapis.com

# Enable Cloud Build API
gcloud services enable cloudbuild.googleapis.com

# Enable Container Registry API
gcloud services enable containerregistry.googleapis.com
```

## 🗄️ Step 2: Set Up Cloud SQL (PostgreSQL)

### 2.1 Create Cloud SQL Instance

```bash
# Create PostgreSQL instance
gcloud sql instances create tapme-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --root-password=YOUR_SECURE_PASSWORD

# Note: db-f1-micro is free tier (shared CPU, 0.6GB RAM)
# For production, use db-custom-1-3840 (1 vCPU, 3.75GB RAM) ~$50/month
```

### 2.2 Create Database

```bash
# Create database
gcloud sql databases create tapme --instance=tapme-db

# Create user
gcloud sql users create tapme-user \
  --instance=tapme-db \
  --password=YOUR_USER_PASSWORD
```

### 2.3 Get Connection Name

```bash
# Get connection name (needed for Cloud Run)
gcloud sql instances describe tapme-db --format="value(connectionName)"
# Output: PROJECT_ID:REGION:INSTANCE_NAME
```

## 🔥 Step 3: Set Up Firebase Cloud Messaging

### 3.1 Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project"
3. Select your Google Cloud project (tapme-backend)
4. Enable Google Analytics (optional)

### 3.2 Get Service Account Key

1. In Firebase Console, go to **Project Settings** > **Service Accounts**
2. Click **Generate New Private Key**
3. Download the JSON file
4. **Important**: Keep this file secure! Never commit it to git.

### 3.3 Add Firebase to Backend

The JSON file contains credentials. You'll add it as an environment variable in Cloud Run.

## 🐳 Step 4: Build and Deploy to Cloud Run

### 4.1 Build Container Image

```bash
cd backend

# Set project ID
export PROJECT_ID=$(gcloud config get-value project)

# Build image
gcloud builds submit --tag gcr.io/$PROJECT_ID/tapme-backend
```

### 4.2 Deploy to Cloud Run

```bash
# Deploy with Cloud SQL connection
gcloud run deploy tapme-backend \
  --image gcr.io/$PROJECT_ID/tapme-backend \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --add-cloudsql-instances PROJECT_ID:REGION:INSTANCE_NAME \
  --set-env-vars "NODE_ENV=production" \
  --set-env-vars "DATABASE_URL=postgresql://tapme-user:PASSWORD@/tapme?host=/cloudsql/PROJECT_ID:REGION:INSTANCE_NAME" \
  --set-env-vars "JWT_SECRET=your-jwt-secret-key" \
  --set-secrets "FIREBASE_SERVICE_ACCOUNT=firebase-service-account:latest"
```

### 4.3 Set Firebase Service Account Secret

```bash
# Create secret from JSON file
gcloud secrets create firebase-service-account \
  --data-file=path/to/service-account-key.json

# Grant Cloud Run access
gcloud secrets add-iam-policy-binding firebase-service-account \
  --member="serviceAccount:PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

### 4.4 Update Deployment to Use Secret

```bash
gcloud run services update tapme-backend \
  --region us-central1 \
  --update-secrets FIREBASE_SERVICE_ACCOUNT=firebase-service-account:latest
```

## 🔧 Step 5: Configure Environment Variables

### 5.1 Set All Environment Variables

```bash
gcloud run services update tapme-backend \
  --region us-central1 \
  --set-env-vars "NODE_ENV=production" \
  --set-env-vars "JWT_SECRET=your-super-secret-jwt-key" \
  --set-env-vars "JWT_EXPIRES_IN=30d" \
  --set-env-vars "RATE_LIMIT_WINDOW_MS=3600000" \
  --set-env-vars "RATE_LIMIT_MAX_REQUESTS=100" \
  --set-env-vars "GOOGLE_CLOUD_PROJECT=$PROJECT_ID" \
  --set-env-vars "GOOGLE_CLOUD_REGION=us-central1"
```

### 5.2 Get Service URL

```bash
# Get the Cloud Run URL
gcloud run services describe tapme-backend \
  --region us-central1 \
  --format="value(status.url)"

# Example: https://tapme-backend-xxxxx-uc.a.run.app
```

## 📱 Step 6: Update Android App

### 6.1 Add Firebase to Android

1. In Firebase Console, add Android app
2. Download `google-services.json`
3. Place in `android-kotlin/app/`

### 6.2 Update build.gradle

```gradle
// Add to android-kotlin/app/build.gradle
plugins {
    id 'com.google.gms.google-services'
}

dependencies {
    implementation 'com.google.firebase:firebase-messaging:23.4.0'
}
```

### 6.3 Update API URL

```gradle
buildConfigField "String", "API_BASE_URL", "\"https://tapme-backend-xxxxx-uc.a.run.app/api/\""
```

## 🎯 Step 7: Test Deployment

### 7.1 Health Check

```bash
curl https://your-cloud-run-url/health
# Should return: {"status":"ok","timestamp":"..."}
```

### 7.2 Test API

```bash
# Register user
curl -X POST https://your-cloud-run-url/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"test-device-123","fcmToken":"test-token"}'
```

## 💰 Cost Estimates

### Free Tier (First 3 Months)
- **Cloud Run**: 2 million requests/month free
- **Cloud SQL**: db-f1-micro free (limited)
- **FCM**: Free, unlimited
- **Total**: $0/month (within free tier)

### Production (After Free Tier)
- **Cloud Run**: ~$0.40 per million requests
- **Cloud SQL**: db-f1-micro: Free (or db-custom-1-3840: ~$50/month)
- **FCM**: Free
- **Cloud CDN**: ~$0.08/GB
- **Total**: ~$5-50/month (depending on usage)

## ⚡ Performance Optimizations

### 1. Enable Cloud CDN

```bash
# Create backend service
gcloud compute backend-services create tapme-backend-service \
  --global

# Add Cloud Run as backend
gcloud compute backend-services add-backend tapme-backend-service \
  --global \
  --network-endpoint-group=CLOUD_RUN_NEG \
  --network-endpoint-group-region=us-central1

# Create URL map and HTTP(S) proxy
# (Use Cloud Console for easier setup)
```

### 2. Enable HTTP/2

Cloud Run automatically uses HTTP/2 ✅

### 3. Set Min Instances (Reduce Cold Starts)

```bash
gcloud run services update tapme-backend \
  --region us-central1 \
  --min-instances=1
```

**Cost**: ~$10/month for 1 instance always running

### 4. Use Cloud Memorystore (Redis) for Caching

```bash
# Create Redis instance
gcloud redis instances create tapme-cache \
  --size=1 \
  --region=us-central1 \
  --tier=basic

# Update environment variable
gcloud run services update tapme-backend \
  --set-env-vars "REDIS_URL=redis://REDIS_IP:6379"
```

## 🔒 Security Best Practices

### 1. Use Secret Manager

Already configured for Firebase service account ✅

### 2. Enable VPC Connector (Optional)

For private Cloud SQL access:
```bash
gcloud compute networks vpc-access connectors create tapme-connector \
  --region=us-central1 \
  --network=default \
  --range=10.8.0.0/28
```

### 3. Set Up IAM

```bash
# Grant Cloud Run service account access to Cloud SQL
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/cloudsql.client"
```

## 📊 Monitoring

### 1. Cloud Run Logs

```bash
# View logs
gcloud logging read "resource.type=cloud_run_revision" --limit 50
```

### 2. Set Up Alerts

In Cloud Console:
1. Go to Monitoring > Alerting
2. Create alert for:
   - High error rate
   - High latency
   - Low availability

## 🚨 Troubleshooting

### Cloud Run Won't Start
- Check logs: `gcloud run services logs read tapme-backend`
- Verify environment variables
- Check Cloud SQL connection

### Database Connection Fails
- Verify Cloud SQL instance is running
- Check connection name format
- Verify Cloud Run has Cloud SQL access

### FCM Not Working
- Verify service account JSON is correct
- Check Firebase project ID matches
- Verify Android app is registered in Firebase

## ✅ Deployment Checklist

- [ ] Google Cloud project created
- [ ] Billing enabled
- [ ] APIs enabled
- [ ] Cloud SQL instance created
- [ ] Database and user created
- [ ] Firebase project created
- [ ] Service account key downloaded
- [ ] Container image built
- [ ] Cloud Run service deployed
- [ ] Environment variables set
- [ ] Secrets configured
- [ ] Health check passing
- [ ] Android app updated
- [ ] API URL configured
- [ ] FCM working

## 🎉 Result

**Deployed to Google Cloud:**
- ✅ Serverless (scales automatically)
- ✅ Low latency (global edge network)
- ✅ Free push notifications (FCM)
- ✅ Managed database
- ✅ Pay only for what you use

**Expected Performance:**
- API Latency: <50ms (with CDN)
- Push Delivery: <1s
- Uptime: 99.95% SLA
- Cost: ~$5-50/month

---

**Next Steps:**
1. Follow this guide step by step
2. Test the deployment
3. Monitor performance
4. Scale as needed
