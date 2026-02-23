# Azure App Service Deployment Guide

## Prerequisites
- Azure Account (free tier available at https://azure.microsoft.com/free)
- Azure CLI installed (https://docs.microsoft.com/cli/azure/install-azure-cli)
- Docker installed (optional, for local testing)
- GitHub account

## Step-by-Step Deployment Instructions

### Step 1: Create Azure Resources

1. **Sign in to Azure Portal**
   - Go to https://portal.azure.com
   - Sign in with your Azure account

2. **Create a Resource Group**
   ```bash
   az group create --name kanso-rg --location eastus
   ```

3. **Create an App Service Plan**
   ```bash
   az appservice plan create \
     --name kanso-plan \
     --resource-group kanso-rg \
     --sku B1 \
     --is-linux
   ```

4. **Create Azure Container Registry (ACR)**
   ```bash
   az acr create \
     --resource-group kanso-rg \
     --name kansoregistry \
     --sku Basic
   ```

5. **Create App Service with Docker Image**
   ```bash
   az webapp create \
     --name kanso-app \
     --resource-group kanso-rg \
     --plan kanso-plan \
     --deployment-container-image-name-user kanso:latest
   ```

### Step 2: Configure GitHub Secrets

1. Go to your GitHub repository settings
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Add these secrets:
   - `AZURE_APP_NAME`: `kanso-app`
   - `AZURE_REGISTRY_LOGIN_SERVER`: Get from ACR (use `az acr show --name kansoregistry`)
   - `AZURE_REGISTRY_USERNAME`: Get from ACR
   - `AZURE_REGISTRY_PASSWORD`: Get from ACR Access Keys
   - `AZURE_PUBLISH_PROFILE`: Download from App Service (Overview → Get Publish Profile)

### Step 3: Manual Deployment (Without GitHub Actions)

If you prefer to deploy manually without CI/CD:

1. **Build Docker Image**
   ```bash
   docker build -t kanso:v1.0 .
   ```

2. **Authentication with ACR**
   ```bash
   az acr login --name kansoregistry
   ```

3. **Tag and Push Image**
   ```bash
   docker tag kanso:v1.0 kansoregistry.azurecr.io/kanso:latest
   docker push kansoregistry.azurecr.io/kanso:latest
   ```

4. **Deploy to App Service**
   ```bash
   az webapp config container set \
     --name kanso-app \
     --resource-group kanso-rg \
     --docker-custom-image-name kansoregistry.azurecr.io/kanso:latest \
     --docker-registry-server-url https://kansoregistry.azurecr.io
   ```

### Step 4: Verify Deployment

1. Go to https://portal.azure.com
2. Navigate to your App Service (kanso-app)
3. Check **Deployments** for deployment status
4. Access your app at: `https://kanso-app.azurewebsites.net`

### Step 5: Troubleshooting

**Check logs:**
```bash
az webapp log tail --name kanso-app --resource-group kanso-rg
```

**Restart the app:**
```bash
az webapp restart --name kanso-app --resource-group kanso-rg
```

**Delete resources (when done):**
```bash
az group delete --name kanso-rg
```

## Monitoring & Scaling

1. **Enable Application Insights**
   - App Service → Application Insights → Enable
   - Monitor application performance and logs

2. **Scale Up (Increase Resources)**
   - App Service Plan → Scale up
   - Choose appropriate SKU for your needs

3. **Auto-Scaling**
   - Configure auto-scale rules based on CPU, memory, or request count

## Estimated Costs

- **App Service Plan B1**: ~$12/month
- **Container Registry Basic**: ~$5/month
- **Total**: ~$17/month minimum

Free tier students/educators can use Azure for Students (free credits).

## Additional Resources

- [Azure App Service Documentation](https://docs.microsoft.com/azure/app-service/)
- [Docker in Azure](https://docs.microsoft.com/azure/container-registry/)
- [GitHub Actions + Azure Integration](https://docs.microsoft.com/azure/developer/github/)
