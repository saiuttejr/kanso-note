# Deploying Kanso to Google Cloud Run

This guide will help you deploy the Kanso Personal Finance Tracker to Google Cloud Run in just a few minutes.

## Prerequisites

1. Google Cloud Account (free tier eligible)
2. Google Cloud SDK installed ([Download](https://cloud.google.com/sdk/docs/install))
3. Docker installed (optional if using Cloud Build)
4. GitHub repository linked to Google Cloud

## Step-by-Step Deployment

### Option 1: Using Cloud Build (Recommended - Easiest)

Cloud Build automatically builds and deploys your app when you push to GitHub.

#### 1. Set Up Google Cloud Project

```bash
# Create a new project or use existing
gcloud projects create kanso-app --name="Kanso"

# Set as active project
gcloud config set project kanso-app

# Enable required APIs
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable artifactregistry.googleapis.com
gcloud services enable compute.googleapis.com
```

#### 2. Connect GitHub Repository

```bash
# Authenticate with Google Cloud
gcloud auth login

# Connect your GitHub repository
gcloud builds connect --repository-name=kanso-note --repository-owner=saiuttejr --region=us-central1
```

#### 3. Create Cloud Build Trigger

```bash
# This creates automatic deployment on push
gcloud builds triggers create github \
  --name=kanso-deploy \
  --repo-name=kanso-note \
  --repo-owner=saiuttejr \
  --branch-pattern='^main$' \
  --build-config=cloudbuild.yaml
```

**Now every push to `main` branch will automatically deploy!**

---

### Option 2: Manual Deployment with gcloud

#### 1. Authenticate

```bash
gcloud auth login
gcloud config set project kanso-app
```

#### 2. Deploy Directly

```bash
# From project root directory
gcloud run deploy kanso \
  --source . \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --memory 512Mi \
  --cpu 1 \
  --timeout 3600
```

#### 3. Get Your Live URL

```bash
gcloud run services describe kanso --region us-central1
```

You'll see output like:
```
Service URL: https://kanso-xxxxx-uc.a.run.app
```

---

### Option 3: Using Docker Locally

#### 1. Build Locally

```bash
docker build -t kanso:latest .
```

#### 2. Push to Container Registry

```bash
# Tag the image
docker tag kanso:latest gcr.io/kanso-app/kanso:latest

# Push to GCR
docker push gcr.io/kanso-app/kanso:latest

# Deploy to Cloud Run
gcloud run deploy kanso \
  --image gcr.io/kanso-app/kanso:latest \
  --region us-central1 \
  --allow-unauthenticated \
  --memory 512Mi
```

---

## Configuration

### Environment Variables

Set environment variables for your deployed instance:

```bash
gcloud run deploy kanso \
  --set-env-vars="KEY1=value1,KEY2=value2" \
  --region us-central1
```

### Memory & CPU Options

```bash
gcloud run deploy kanso \
  --memory 1Gi \           # 512Mi, 1Gi, 2Gi, 4Gi
  --cpu 2 \               # 1 or 2
  --timeout 3600 \        # in seconds
  --region us-central1
```

### Custom Domain

```bash
gcloud run domain-mappings create \
  --service=kanso \
  --domain=your-domain.com \
  --region=us-central1
```

---

## Free Tier Limits

✅ **Always included per month:**
- 2 million requests
- 360,000 GB-seconds of compute
- 1 GB of outbound data transfer

✅ **After free tier:**
- $0.00002400 per request
- $0.00001667 per GB-second
- $0.123 per GB outbound data transfer

---

## Monitoring & Logs

### View Logs

```bash
gcloud run logs read kanso --region us-central1 --limit 50
```

### View Metrics

```bash
# Open Cloud Console
gcloud run services describe kanso --region us-central1
```

### Set Alerts

Via Google Cloud Console:
- Go to Cloud Run → kanso → Metrics
- Create alerts for CPU, memory, latency

---

## Scaling

Cloud Run auto-scales based on traffic:
- **Min instances:** 0 (scales down when idle)
- **Max instances:** 100 (default)

### Configure Scaling

```bash
gcloud run deploy kanso \
  --min-instances 1 \     # Keeps 1 instance warm
  --max-instances 10 \
  --region us-central1
```

---

## Troubleshooting

### App won't start

Check logs:
```bash
gcloud run logs read kanso --region us-central1 --limit 100
```

### Port issues

Ensure `application.properties` has:
```properties
server.port=${PORT:8080}
```

### Memory issues

Increase memory:
```bash
gcloud run deploy kanso --memory 1Gi --region us-central1
```

### Cold starts

Set `--min-instances 1` to keep instance warm (minimal cost)

---

## Updating Your App

### Automatic (with Cloud Build)

Just push to main branch:
```bash
git add .
git commit -m "Update features"
git push origin main
```

Cloud Build will automatically rebuild and deploy!

### Manual

```bash
gcloud run deploy kanso --source . --region us-central1
```

---

## Cost Estimation

For typical usage:
- **Free tier:** $0/month (up to 2M requests)
- **Light usage:** $1-5/month (10-50M requests)
- **Medium usage:** $5-20/month (50-500M requests)

---

## Next Steps

1. Deploy using one of the options above
2. Test your app at the provided URL
3. Set up custom domain
4. Enable monitoring and alerts
5. Configure auto-scaling if needed

## Support

- [Cloud Run Documentation](https://cloud.google.com/run/docs)
- [Pricing Calculator](https://cloud.google.com/products/calculator)
- [Cloud Run GitHub Actions](https://github.com/google-github-actions/deploy-cloudrun)

---

**Kanso is now ready for Google Cloud Run!** 🚀
