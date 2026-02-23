# Kanso
## Personal Finance Tracker
### By Sai Uttej R

A Java Spring Boot web application for intelligent personal finance management with:
- CSV upload/import
- manual transaction entry
- one-click transaction data reset
- rule-based auto-categorization
- monthly income/expense trends
- unusual expense flagging

## Tech Stack

- Java 17
- Spring Boot 3
- Thymeleaf
- Maven

## Run Locally

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```
![alt text](image.png)

## Deployment

### Azure App Service
For complete Azure deployment instructions with GitHub Actions CI/CD, see [AZURE_DEPLOYMENT.md](./AZURE_DEPLOYMENT.md)

**Quick Deploy:**
```bash
# 1. Create Azure resources
az group create --name kanso-rg --location eastus
az appservice plan create --name kanso-plan --resource-group kanso-rg --sku B1 --is-linux
az acr create --resource-group kanso-rg --name kansoregistry --sku Basic

# 2. Build and push Docker image
docker build -t kansoregistry.azurecr.io/kanso:latest .
az acr login --name kansoregistry
docker push kansoregistry.azurecr.io/kanso:latest

# 3. Deploy to App Service
az webapp create --name kanso-app --resource-group kanso-rg --plan kanso-plan
az webapp config container set --name kanso-app --resource-group kanso-rg \
  --docker-custom-image-name kansoregistry.azurecr.io/kanso:latest
```

Access your app at: `https://kanso-app.azurewebsites.net`

## How to Use

1. **Upload Transactions**
   - Click the "Upload CSV" button on the dashboard
   - Select a CSV file with your transaction data
   - The app will import and auto-categorize your transactions

2. **Add Manual Transactions**
   - Use the "Add Transaction" form on the dashboard
   - Enter the date, description, and amount
   - Leave category blank for manual entry, or it will auto-categorize based on rules

3. **View Dashboard Statistics**
   - See total transactions count
   - Track total income and expenses
   - Monitor your net flow (income - expenses)

4. **Review Categorized Transactions**
   - All transactions are listed with their categories
   - The rule engine automatically categorizes based on keywords
   - Unusual transactions are highlighted with a warning icon

5. **Track Monthly Trends**
   - View income and expense trends by month
   - Analyze spending patterns over time
   - Identify seasonal variations in your finances

6. **Manage Category Rules**
   - Add custom keyword rules to auto-categorize transactions
   - Rules are applied based on keywords in the transaction description
   - Higher priority is given to custom rules over built-in rules

## CSV Format

Upload a CSV with headers:

```csv
date,description,amount,category
2026-02-01,Salary,4200.00,Income
2026-02-02,Walmart Grocery,-78.31,
```

Notes:
- `category` is optional
- supported date formats include `yyyy-MM-dd`, `MM/dd/yyyy`, and `dd-MM-yyyy`
- use negative amounts for expenses and positive amounts for income

## Rule Engine

- Built-in keyword rules (for groceries, utilities, transport, etc.) are included.
- You can add custom keyword rules from the dashboard.
- Custom rules are checked before default rules.

## Unusual Transaction Detection

Expenses are flagged as unusual when they exceed:

`mean(expense_amount) + 2 * stddev(expense_amount)`

This is calculated from currently loaded expense transactions.

## Current Scope

- In-memory data only (no database yet)
- Single-user local usage
- Rule persistence resets on app restart
- "Clear Transaction Data" removes all loaded transactions but keeps rules

## Suggested Next Iterations

1. Persist data/rules to PostgreSQL.
2. Add user authentication.
3. Add charts with a JS library (Chart.js/ECharts).
4. Add recurring transaction detection.
