# 🎯 Kanso Personal Finance Tracker — New User Onboarding Guide

Welcome to **Kanso**, your personal finance companion! This guide will walk you through everything you need to get started, from installation to tracking your first transactions.

---

## 📋 Table of Contents

1. [What is Kanso?](#what-is-kanso)
2. [Quick Start (5 Minutes)](#quick-start-5-minutes)
3. [System Requirements](#system-requirements)
4. [Installation Step-by-Step](#installation-step-by-step)
5. [Running the Application](#running-the-application)
6. [Your First Visit](#your-first-visit)
7. [Core Concepts](#core-concepts)
8. [Feature Guide](#feature-guide)
9. [Common Tasks](#common-tasks)
10. [Tips & Tricks](#tips--tricks)
11. [Troubleshooting](#troubleshooting)
12. [FAQ](#faq)

---

## What is Kanso?

**Kanso** is a personal finance tracker that runs 100% offline on your computer. It helps you:

✅ **Track spending** — Add transactions manually or import from CSV  
✅ **Understand patterns** — See monthly trends and top spending categories  
✅ **Budget intelligently** — Set limits per category and get alerts  
✅ **Detect anomalies** — Find unusual expenses automatically  
✅ **Identify recurring costs** — Spot subscriptions and recurring bills  
✅ **Auto-categorize** — Smart rules that assign categories to transactions  
✅ **Stay private** — All data stays on your computer, no cloud required

### Key Philosophy
- **Offline-first**: Works without internet
- **Simple**: Single dashboard, no complexity
- **Secure**: Uses military-grade AES-256 encryption
- **Deterministic**: Same input always produces the same result

---

## Quick Start (5 Minutes)

### If you already have Java & Maven:

```bash
# 1. Navigate to the project folder
cd BankingOOP

# 2. Build the application
mvn clean install

# 3. Run the application
mvn spring-boot:run

# 4. Open your browser and visit
# http://localhost:8080
```

That's it! You'll see the dashboard.

### New to Java? See [Installation Step-by-Step](#installation-step-by-step) below.

---

## System Requirements

Before you start, make sure your computer has:

| Requirement | What You Need |
|-------------|---------------|
| **Java** | JDK 17 or higher (Java 21 recommended) |
| **Maven** | Version 3.6 or higher |
| **RAM** | At least 512MB free (2GB recommended) |
| **Disk Space** | 500MB for the app and dependencies |
| **Port 8080** | Must be available (the app uses this port) |
| **Browser** | Chrome, Firefox, Safari, or Edge |

### Verify Your Setup

Open Command Prompt/Terminal and run:

```bash
java -version
mvn -version
```

You should see version information. If not, you need to install Java and Maven.

---

## Installation Step-by-Step

### Step 1: Install Java (if not already installed)

**For Windows:**
1. Go to [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/#java17) or use [OpenJDK](https://adoptium.net/)
2. Download Java 17 or 21 (LTS versions recommended)
3. Run the installer and follow the prompts
4. Restart your computer

**For macOS/Linux:**
```bash
# macOS (using Homebrew)
brew install openjdk@17

# Linux (Ubuntu/Debian)
sudo apt-get install openjdk-17-jdk
```

### Step 2: Install Maven (if not already installed)

**For Windows:**
1. Go to [Apache Maven Downloads](https://maven.apache.org/download.cgi)
2. Download the binary zip file
3. Extract to a folder (e.g., `C:\Users\YourName\maven`)
4. Add maven to your PATH:
   - Right-click "This PC" → Properties → Advanced system settings
   - Click "Environment Variables"
   - Under "User variables", click "New"
   - Name: `M2_HOME`, Value: `C:\Users\YourName\maven\apache-maven-3.9.13`
   - Click "OK"

**For macOS/Linux:**
```bash
# macOS (using Homebrew)
brew install maven

# Linux (Ubuntu/Debian)
sudo apt-get install maven
```

### Step 3: Download Kanso

1. Download the Kanso project (or clone from Git if available)
2. Extract the zip file to a folder on your computer
3. Navigate to the folder in Command Prompt/Terminal:
   ```bash
   cd path/to/BankingOOP
   ```

### Step 4: Build the Application

```bash
mvn clean install
```

This will:
- Download all dependencies (first time takes 2-5 minutes)
- Compile the Java code
- Run tests
- Package the application

Wait for it to finish. You should see `BUILD SUCCESS` at the end.

---

## Running the Application

### Method 1: Maven Command (Recommended)

```bash
mvn spring-boot:run
```

You'll see startup messages. Wait for:
```
Started PersonalFinanceTrackerApplication in X.XXX seconds
```

Then open your browser to: **http://localhost:8080**

### Method 2: From an IDE (IntelliJ, VS Code, Eclipse)

1. Open the project in your IDE
2. Find `PersonalFinanceTrackerApplication.java` in the file explorer
3. Right-click on it → "Run" (or use keyboard shortcut)
4. Open your browser to **http://localhost:8080**

### Stop the Application

Press `Ctrl+C` in the terminal where the app is running.

---

## Your First Visit

When you open **http://localhost:8080**, you'll see:

```
┌──────────────────────────────────────────────┐
│  💰 Personal Finance Tracker                 │
│                                              │
│  Total Income:    $0.00                      │
│  Total Expenses:  $0.00                      │
│  Net Flow:        $0.00                      │
│                                              │
│  [Add Transaction] [Import CSV] [Load Sample]│
└──────────────────────────────────────────────┘
```

### Try Loading Sample Data First

Click **"Load Sample Data"** to see the app in action with example transactions. This will:
- Load 100+ sample transactions across different categories
- Show you monthly trends, spending patterns, and anomalies
- Demonstrate all the features

---

## Core Concepts

### 1. **Transactions**
A single financial exchange: a payment you made or income you received.

**Example:** "Costco groceries - $125.50" (expense) or "Salary deposit" (income)

### 2. **Categories**
Labels that group similar transactions (e.g., "Groceries", "Utilities", "Entertainment").

Each transaction belongs to one category. The app auto-assigns categories using rules.

### 3. **Categories**
Rules that automatically assign categories based on keywords in the transaction description.

**Example:** If description contains "WHOLE FOODS" → auto-categorize as "Groceries"

### 4. **Budget**
A monthly spending limit for a specific category.

**Example:** Budget of $500/month for "Groceries" alerts you when you exceed 80% ($400).

### 5. **Anomaly**
An unusual transaction that's statistically different from your normal spending.

The app flags transactions that are more than 2 standard deviations above average.

---

## Feature Guide

### 📊 Dashboard Overview

The main page shows you:

| Section | What It Shows |
|---------|---------------|
| **Summary Cards** | Total income, expenses, net flow, and savings rate |
| **Recent Transactions** | Your last 20 transactions with date, description, amount, category |
| **Monthly Trends** | Income and expenses by month with month-over-month changes |
| **Top Categories** | Your top 3 spending categories |
| **Unusual Transactions** | Flagged transactions that are statistical outliers |
| **Recurring Transactions** | Transactions that appear multiple times (subscriptions, rent, etc.) |

### ➕ Add a Transaction

1. Click **"Add Manual Transaction"**
2. Fill in:
   - **Date**: YYYY-MM-DD format (e.g., 2026-03-15)
   - **Description**: What you bought (e.g., "Coffee at Starbucks")
   - **Amount**: Use negative for expenses, positive for income
   - **Category** (optional): The app will auto-guess if you leave it blank
3. Click **"Add"**

**Example:**
```
Date:        2026-03-09
Description: Target shopping
Amount:      -47.99
Category:    (auto-assign)
```

### 📤 Import from CSV

If you have a bank statement in CSV format:

1. Click **"Import Transactions"**
2. Select your CSV file
3. Click **"Upload"**
4. The app will parse and import all transactions

**CSV Format Expected:**
```
Date,Description,Amount,Category
2026-03-01,Salary,-5000.00,Income
2026-03-02,Whole Foods,125.50,Groceries
2026-03-05,Electric Bill,89.75,Utilities
```

### 💰 Set a Budget

1. Scroll to the **"Budgets"** section
2. Click **"Add Budget"**
3. Fill in:
   - **Category**: Which category to budget (e.g., "Groceries")
   - **Monthly Limit**: Maximum spend per month
   - **Alert Threshold**: Alert when you hit this % of the limit (default 80%)
4. Click **"Create Budget"**

**Result:** You'll get alerts when approaching or exceeding your limits.

### 🤖 Auto-Categorization Rules

The app automatically categorizes transactions using rules:

**Default Rules** are built-in and categorize common transactions:
- "WHOLE FOODS" → Groceries
- "SHELL GAS" → Gas
- "NETFLIX" → Entertainment

**Custom Rules** are yours to create:

1. Scroll to the **"Rules"** section
2. Click **"Add Rule"**
3. Fill in:
   - **Pattern**: Keyword to match in description
   - **Category**: Which category to assign
4. Click **"Add Rule"**

**How It Works:**
- Rule matches the description text (case-insensitive)
- Multiple matching rules? Highest priority wins
- No matching rule? Transaction stays "Uncategorized"

### 📈 Analytics & Insights

The dashboard automatically shows:

**Monthly Trends**
- Income and expenses for each month
- Month-over-month (MoM) change as a percentage
- 3-month rolling average of expenses

**Top Spending Categories**
- Your top 3 spending categories
- Helps you understand where money goes

**Unusual Transactions**
- Transactions > 2 standard deviations from average
- Helps catch fraud or unexpected spending
- Click to review or edit

**Recurring Transactions**
- Subscriptions and recurring bills detected
- Shows frequency (monthly, bi-weekly, etc.)
- Useful for identifying recurring costs

### 💾 Export Data

1. Click **"Export as CSV"**
2. Optionally select date range (from/to)
3. Click **"Download"**
4. CSV file downloads to your computer (import to Excel, etc.)

### 🔄 Undo

Made a mistake? Click **"Undo"** to revert the last bulk action (add/import).

**Note:** Undo only works for the most recent action.

---

## Common Tasks

### Task 1: Import Bank Statements

**Scenario:** You have CSV exports from your bank.

**Steps:**
1. Click "Import Transactions"
2. Select your CSV file
3. Click "Upload"
4. Review the imported transactions on the dashboard
5. Adjust any mis-categorized transactions manually

**Tips:**
- Most banks let you export transactions as CSV from their website
- The app accepts multiple date formats: yyyy-MM-dd, MM/dd/yyyy, dd-MM-yyyy
- Check that amounts are correct (expenses should be negative)

### Task 2: Understand Your Spending

**Scenario:** You want to know where your money goes.

**Steps:**
1. Look at the **"Top 3 Spending Categories"** section
2. Click on a category to filter transactions
3. See the **"Monthly Trends"** chart for weekly patterns
4. Compare your **"Savings Rate"** to see if you're on track

**Quick Insights:**
- Red net flow = Spending more than you earn
- Green net flow = You're saving money
- Savings rate > 20% is healthy

### Task 3: Set & Track Budgets

**Scenario:** You want to limit grocery spending to $500/month.

**Steps:**
1. Click "Add Budget"
2. Category: "Groceries"
3. Monthly Limit: "500"
4. Alert Threshold: "80" (alert at $400)
5. Click "Create Budget"

**During the Month:**
- Dashboard shows "Budget Status" card
- If you hit 80%, you get a warning
- If you exceed 100%, it turns red

### Task 4: Create Auto-Categorization Rules

**Scenario:** Every Whole Foods purchase should be "Groceries", not "Shopping".

**Steps:**
1. Go to the **"Rules"** section
2. Click "Add Rule"
3. Pattern: "WHOLE FOODS"
4. Category: "Groceries"
5. Click "Add Rule"

**Result:**
- Future transactions with "WHOLE FOODS" auto-categorize as "Groceries"
- Re-import or re-categorize old transactions to apply the rule

### Task 5: Detect Unusual Spending

**Scenario:** You want to spot suspicious or unusual transactions.

**Steps:**
1. Look at the **"Unusual Transactions"** section
2. The app automatically flags spending > 2 standard deviations
3. Click a transaction to review or edit
4. Delete if it's a data entry error

**Example:**
- Normal coffee: $5-7
- Unusual: $57 coffee purchase → gets flagged

---

## Tips & Tricks

### 💡 Pro Tips

**1. Use Consistent Descriptions**
- Use the same format for similar transactions
- Example: Always "WHOLE FOODS MARKET" not "Whole Foods" and "WF Market"
- Better descriptions = Better rule matching and recurring detection

**2. Set Realistic Budgets**
- Review your actual spending first (use monthly trends)
- Set budgets 10-20% above average to account for variance
- Too-tight budgets create alert fatigue

**3. Create Rules for Recurring Spending**
- Identify all your subscriptions and recurring bills
- Create rules for each (Netflix, Spotify, Rent, etc.)
- Saves time on categorization

**4. Review Monthly Anomalies**
- Unusual transactions aren't always fraud
- Some might be legitimate one-time spending (car repair, gifts, travel)
- Building a habit of review strengthens financial awareness

**5. Use Custom Categories**
- Create categories that matter to YOUR spending
- Example: "Hobbies", "Health", "Pets" if those matter to you
- Not just generic categories

**6. Archive Old Data**
- Export your data regularly as CSV backup
- The app stores everything locally (no cloud backups)
- Good practice: monthly exports for redundancy

### 🎯 Workflow Tips

**Weekly:**
- Add any manual transactions not auto-imported
- Glance at the dashboard for anomalies
- Update budgets if needed

**Monthly:**
- Review "Recurring Transactions" section
- Check if top spending categories align with your goals
- Look at "Monthly Trends" to spot patterns
- Export data as backup

**Quarterly:**
- Review all your rules — are they still accurate?
- Adjust budgets based on actual spending
- Set goals for next quarter (e.g., "Reduce dining out by 20%")

---

## Troubleshooting

### ❌ Application Won't Start

**Problem:** `mvn spring-boot:run` fails or the app crashes on startup.

**Solutions:**
1. **Check Java version:**
   ```bash
   java -version
   ```
   Must be Java 17 or higher. Download from [adoptium.net](https://adoptium.net) if needed.

2. **Check Maven is installed:**
   ```bash
   mvn -version
   ```
   Download from [maven.apache.org](https://maven.apache.org) if missing.

3. **Clean the build:**
   ```bash
   mvn clean install
   ```
   This removes old files and rebuilds everything.

4. **Check port 8080 is free:**
   ```bash
   # Windows
   netstat -ano | findstr :8080
   
   # macOS/Linux
   lsof -i :8080
   ```
   If another app is using it, either close the app or change the port in `application.properties`.

5. **Check database:**
   ```bash
   # Delete corrupted data and start fresh
   rm -rf ./data
   mkdir ./data
   ```
   Then restart the app.

### ❌ Cannot Open http://localhost:8080

**Problem:** Browser says "connection refused" or "cannot reach server".

**Solutions:**
1. Make sure the app is running (you should see startup messages in the terminal)
2. Wait 10-15 seconds after seeing "Started PersonalFinanceTrackerApplication"
3. Try refreshing the browser (Ctrl+R or Cmd+R)
4. Make sure you're using `http://` not `https://` (no SSL)
5. Try a different browser (Chrome, Firefox, Safari)

### ❌ CSV Import Fails

**Problem:** "Invalid CSV format" error when uploading.

**Solutions:**
1. **Check the CSV format:**
   - Must have headers: Date, Description, Amount, Category
   - Date format: yyyy-MM-dd (or MM/dd/yyyy or dd-MM-yyyy)
   - Amount: Use negative for expenses, positive for income
   - All fields required (but Category can be empty)

2. **Check for special characters:**
   - CSV may have encoding issues with non-ASCII characters
   - Try saving CSV as "UTF-8" encoding in Excel

3. **Check file size:**
   - Max file size is 5MB
   - If your CSV is larger, split it into multiple files

4. **Example valid CSV:**
   ```
   Date,Description,Amount,Category
   2026-03-01,Salary,5000.00,Income
   2026-03-05,Whole Foods,-125.50,
   2026-03-10,Electric Bill,-89.75,Utilities
   ```

### ❌ Transactions Not Auto-Categorizing

**Problem:** Transactions stay "Uncategorized" even after adding rules.

**Solutions:**
1. **Rule might not match the description**
   - Check the actual transaction description (case matters sometimes)
   - Pattern is case-insensitive but must be exact substring
   - Example: "WHOLE FOODS" won't match "Whole Foods Market" if you configured "FOODS"

2. **Re-import after creating rules**
   - Rules only apply to NEW transactions
   - Old transactions won't be auto-categorized retroactively
   - You need to manually re-categorize or re-import

3. **Check rule priority**
   - If multiple rules match, the highest priority wins
   - Look in the "Rules" section and adjust priority if needed

### ❌ Budget Alerts Not Showing

**Problem:** You added a budget but don't see alerts.

**Solutions:**
1. Make sure there are transactions in that category
   - Budgets only show if there's spending in the category
   - Auto-categorize transactions first

2. Check your alert threshold
   - Default is 80% of budget
   - You need to hit that % to see the alert
   - You can lower the threshold to get earlier warnings

3. Refresh the page
   - Sometimes the UI needs a page refresh to show updates
   - Press Ctrl+R or Cmd+R

---

## FAQ

### General Questions

**Q: Is my data really 100% offline?**  
A: Yes! All data is stored in a local H2 database file at `./data/kanso-db`. No internet connection is required. No data is sent to any server.

**Q: Can I backup my data?**  
A: The database file is at `./data/kanso-db`. You can copy this file to backup your data. Or use the "Export CSV" feature for a human-readable backup.

**Q: How do I restore from a backup?**  
A: Stop the app, replace `./data/kanso-db` with your backup copy, restart the app.

**Q: Can I move the database to another folder?**  
A: Yes, edit `application.properties` and change `kanso.storage.db-path=./data/kanso-db` to your desired path.

**Q: Is there a mobile app?**  
A: Not yet. Kanso is web-based (desktop/tablet browsers only). Future versions might add mobile support.

---

### Feature Questions

**Q: How accurate is anomaly detection?**  
A: Anomalies use statistical methods (2 standard deviations). It works well with steady, regular spending. Highly variable spending may have false positives.

**Q: How does recurring detection work?**  
A: The app looks for transactions with similar descriptions that happen regularly. It allows 5% variation in amount. Works best with consistent merchant names.

**Q: Can I import from [Bank Name]?**  
A: Most banks support CSV export. Download your statement as CSV, make sure it matches the expected format, and upload it.

**Q: How many transactions can I track?**  
A: Technically unlimited, but performance slows after ~50,000 transactions. Kanso is designed for personal use, not enterprise-scale.

**Q: Can I delete a transaction permanently?**  
A: Yes, click the "Delete" button on any transaction. It's permanent (can't undo deleted transactions).

---

### Technical Questions

**Q: What database does it use?**  
A: H2, a lightweight Java database. It stores data in a local file, no server needed.

**Q: What Java version do I need?**  
A: Java 17 or higher. Java 21 LTS is recommended.

**Q: Can I modify the source code?**  
A: Yes! It's open source. You can customize rules, features, categories, etc.

**Q: How do I update to a newer version?**  
A: Download the latest code, run `mvn clean install` and `mvn spring-boot:run` again.

**Q: Can I run multiple instances?**  
A: Not safely — they'll conflict over the database file. Use one instance per folder.

---

### Troubleshooting Questions

**Q: What if I forgot to re-import after adding a rule?**  
A: Old transactions weren't auto-categorized. Either:
- Re-upload the same CSV (it will re-process)
- Manually edit transactions to the correct category

**Q: How do I reset to a clean state?**  
A: Delete the `./data` folder and restart. This clears all data.

**Q: Can I access the H2 database console?**  
A: Yes, visit `http://localhost:8080/h2-console` while the app is running. Use username `sa` (no password).

**Q: Is it normal for the app to be slow on startup?**  
A: First startup (with dependencies downloading) takes 2-5 minutes. Subsequent startups are faster (10-30 seconds).

---

## Need More Help?

- **Technical Deep-Dive**: See `KANSO_DOCUMENTATION.md` for architecture, algorithms, and advanced topics
- **API Reference**: See `README.md` for REST API endpoints
- **Code Comments**: Every method in the source code has a 1-liner description explaining what it does
- **Sample Data**: Click "Load Sample Data" on the dashboard to see the app in action

---

## What's Next?

🎉 **Congratulations on getting Kanso set up!**

Now that you're onboarded, try:

1. **Load sample data** to see all features in action
2. **Create your first budget** for a category you care about
3. **Import your bank statements** to get real data
4. **Create custom rules** for your recurring transactions
5. **Review your first monthly trend** to understand your spending

Happy tracking! 💰

---

**Version:** 2.0.0  
**Last Updated:** March 2026  
**Author:** Sai Uttej R  
**License:** Proprietary
