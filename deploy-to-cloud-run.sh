#!/bin/bash
# Quick Deploy Script for Google Cloud Run

set -e

echo "🚀 Kanso - Google Cloud Run Deployment"
echo "======================================="

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ Google Cloud SDK not found. Please install it first:"
    echo "   https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "⚠️  Docker not found. Using Cloud Build instead (recommended)"
    BUILD_METHOD="cloud-build"
else
    BUILD_METHOD="docker"
fi

echo ""
echo "Select deployment method:"
echo "1. Cloud Build (recommended - auto-deploys on git push)"
echo "2. Local Docker build"
echo "3. Direct gcloud deploy"
echo ""
read -p "Enter choice (1-3): " choice

case $choice in
    1)
        echo ""
        echo "📋 Cloud Build Setup..."
        echo ""
        read -p "Enter your Google Cloud Project ID: " PROJECT_ID
        read -p "Enter your GitHub username: " GITHUB_USER
        read -p "Enter your GitHub token (create at https://github.com/settings/tokens): " GITHUB_TOKEN
        
        gcloud config set project "$PROJECT_ID"
        gcloud services enable cloudbuild.googleapis.com run.googleapis.com artifactregistry.googleapis.com
        
        echo "✅ APIs enabled"
        echo ""
        echo "Next steps:"
        echo "1. Visit: https://console.cloud.google.com/cloud-build/github/connect"
        echo "2. Connect your GitHub repository (saiuttejr/kanso-note)"
        echo "3. Create a trigger pointing to 'main' branch with 'cloudbuild.yaml'"
        echo ""
        echo "📚 For detailed instructions: cat CLOUD_RUN_DEPLOYMENT.md"
        ;;
    
    2)
        echo ""
        echo "🐳 Building with Docker..."
        read -p "Enter your Google Cloud Project ID: " PROJECT_ID
        
        docker build -t kanso:latest .
        docker tag kanso:latest gcr.io/$PROJECT_ID/kanso:latest
        
        echo ""
        echo "📤 Pushing to Container Registry..."
        docker push gcr.io/$PROJECT_ID/kanso:latest
        
        echo ""
        echo "🚀 Deploying to Cloud Run..."
        gcloud run deploy kanso \
            --image gcr.io/$PROJECT_ID/kanso:latest \
            --region us-central1 \
            --allow-unauthenticated \
            --memory 512Mi \
            --cpu 1 \
            --project $PROJECT_ID
        
        echo ""
        echo "✅ Deployment complete!"
        gcloud run services describe kanso --region us-central1 --project $PROJECT_ID
        ;;
    
    3)
        echo ""
        echo "🚀 Direct deployment..."
        read -p "Enter your Google Cloud Project ID: " PROJECT_ID
        
        gcloud config set project "$PROJECT_ID"
        
        gcloud run deploy kanso \
            --source . \
            --region us-central1 \
            --allow-unauthenticated \
            --memory 512Mi \
            --cpu 1 \
            --timeout 3600
        
        echo ""
        echo "✅ Deployment complete!"
        ;;
    
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac

echo ""
echo "📚 View logs:"
echo "   gcloud run logs read kanso --region us-central1"
echo ""
echo "🔗 Visit your app, view metrics, and manage in Cloud Console:"
echo "   gcloud run services describe kanso --region us-central1"
echo ""
echo "✨ Kanso is now live on Google Cloud Run!"
