# Kanso — Personal Finance Tracker

**Author:** Sai Uttej R  
**Version:** 2.0.0  
**Tech Stack:** Spring Boot 3.4.2 · Java 17 · H2 Database · Thymeleaf · Flyway · Maven  
**License:** Proprietary

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Prerequisites](#prerequisites)
4. [Installation & Setup](#installation--setup)
5. [Running the Application](#running-the-application)
6. [Configuration](#configuration)
7. [Usage Guide](#usage-guide)
8. [Technical Architecture](#technical-architecture)
9. [Database Schema](#database-schema)
10. [API Endpoints & Controllers](#api-endpoints--controllers)
11. [Rule Engine & Categorization](#rule-engine--categorization)
12. [Analytics & Anomaly Detection](#analytics--anomaly-detection)
13. [Security & Encryption](#security--encryption)
14. [Project Structure](#project-structure)
15. [Testing](#testing)
16. [Building & Deployment](#building--deployment)
17. [Troubleshooting](#troubleshooting)
18. [Development Guide](#development-guide)

---

## Project Overview

Kanso is a **single-user, offline-first personal finance tracker** built with Spring Boot 3 and Java 17. It runs entirely on `localhost:8080` with an embedded H2 database stored as a local file. No internet connection or external services are required.

### Core Philosophy

- **Offline-First:** All data stays on your device. No cloud services, APIs, or network calls.
- **Deterministic:** The rule engine always produces identical output for identical input.
- **Explainable:** Every categorization decision is logged with the matching rule, priority, and match length.
- **Simple:** Single-page server-rendered dashboard with zero JavaScript frameworks.
- **Secure:** AES-256-GCM encryption with PBKDF2 key derivation for sensitive data.

### Ideal Use Cases

- Personal expense tracking and budgeting
- CSV import from bank statements
- Understanding monthly spending patterns
- Detecting unusual transactions (anomalies)
- Identifying recurring expenses
- Offline financial analysis (no cloud dependency)

---

## Features

| Feature | Description |
|---------|-------------|
| **Transaction Management** | Add, edit, delete, bulk-import transactions via CSV upload |
| **Auto-Categorization** | Deterministic rule engine with KEYWORD/REGEX patterns and priority-based conflict resolution |
| **Analytics Dashboard** | Monthly income/expense trends with month-over-month (MoM) deltas and 3-month rolling averages |
| **Anomaly Detection** | Statistical outlier flagging using mean + 2σ threshold |
| **Recurring Expenses** | Identifies recurring transactions by normalized description (5% amount tolerance) |
| **Custom Rules** | Add, edit, delete category rules with custom priorities |
| **CSV Export** | Export transactions with optional date-range filtering |
| **Undo** | Single-step undo for the last add/import action |
| **Multiple Date Formats** | Supports `yyyy-MM-dd`, `MM/dd/yyyy`, and `dd-MM-yyyy` |
| **Data Security** | Optional AES-256-GCM encryption for uploaded files |
| **File Audit Trail** | Tracks all uploaded CSV files with metadata |
| **Responsive UI** | Single-page Thymeleaf template with clean, modern CSS |

---

## Prerequisites

### System Requirements

- **Operating System:** Windows, macOS, or Linux
- **Java Version:** JDK 17 or higher (Java 21 recommended for LTS)
- **Maven:** 3.6 or higher
- **RAM:** Minimum 512MB, 2GB recommended
- **Disk Space:** 500MB for application and dependencies
- **Port:** `8080` must be available

### Check Your Java Installation

```bash
java -version
javac -version
mvn -version
```

Expected output (or higher):
```
openjdk version "17.0.1" 2021-10-19 LTS
Apache Maven 3.9.12
```

---

## Installation & Setup

### Step 1: Clone or Download the Project

```bash
cd C:\Users\saiut\Desktop\Haccnt\BankingOOP
```

### Step 2: Verify Project Structure

Ensure the project layout matches the expected structure:

```
BankingOOP/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/bankingoop/finance/
│   │   └── resources/
│   └── test/
├── Dockerfile
└── README.md
```

### Step 3: Build the Project

Download all dependencies and compile the code:

```bash
mvn clean install
```

This will:
- Clear any previous build artifacts (`target/` folder)
- Download all Maven dependencies
- Compile Java source files
- Run all tests
- Package the application

**First-time build may take 2-5 minutes.** Subsequent builds are faster.

### Step 4: Initialize Data Directory

The application automatically creates the data directory on first run:

```
./data/
├── kanso-db    (H2 database file, created automatically)
├── uploads/    (CSV upload directory, created automatically)
└── backups/    (Encrypted backups, if enabled)
```

No manual setup required.

---

## Running the Application

### Starting the Server

```bash
mvn spring-boot:run
```

Or, if you've already built the project:

```bash
java -jar target/kanso-1.0.0.jar
```

### Expected Startup Output

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/

...
2026-03-09 10:15:23.456  INFO 12345 --- [main] c.b.f.PersonalFinanceTrackerApplication : Started PersonalFinanceTrackerApplication in 3.245s (JVM running for 3.456s)
2026-03-09 10:15:23.789  INFO 12345 --- [main] o.s.b.a.w.s.WelcomePageHandlerMapping : Adding welcome page: class path resource [templates/dashboard.html]
```

### Accessing the Application

Once the server starts, open your browser and navigate to:

```
http://localhost:8080
```

You should see the Kanso dashboard with:
- Transaction input form
- List of transactions
- Analytics summary
- Category rules management

### Stopping the Server

Press `Ctrl+C` in the terminal to gracefully shut down the server.

---

## Configuration

All configuration is managed in [src/main/resources/application.properties](src/main/resources/application.properties).

### Key Configuration Properties

#### Server Configuration

```properties
server.address=127.0.0.1          # Bind to localhost only (blocks external access)
server.port=8080                   # Port to listen on
```

#### File Upload Limits

```properties
spring.servlet.multipart.max-file-size=5MB      # Max CSV file size
spring.servlet.multipart.max-request-size=5MB   # Max request payload
```

#### H2 Database Configuration

```properties
# Database file location (persists across restarts)
spring.datasource.url=jdbc:h2:file:./data/kanso-db;DB_CLOSE_ON_EXIT=FALSE

# In-file mode database settings
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Enable H2 console for debugging (localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

#### Hibernate / JPA Configuration

```properties
# Schema validation only (Flyway owns migrations)
spring.jpa.hibernate.ddl-auto=validate

# Format SQL logs for readability
spring.jpa.properties.hibernate.format_sql=true

# Database dialect for H2
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

#### Flyway Migrations

```properties
spring.flyway.enabled=true                         # Enable migration runner
spring.flyway.locations=classpath:db/migration    # Migration file location
```

#### Custom Kanso Settings

```properties
kanso.storage.upload-dir=./data/uploads           # CSV upload directory
kanso.storage.db-path=./data/kanso-db             # Database file path
```

#### Logging

```properties
logging.level.com.bankingoop.finance=DEBUG         # App-level debug logging
logging.level.org.flywaydb=INFO                    # Flyway migration info
```

### Environment-Specific Configuration

Create profile-specific configuration files for different environments:

- **Development** (default): `application.properties`
- **Testing**: `application-test.properties` (used during `mvn test`)
- **Production**: Create `application-prod.properties`

To use a specific profile:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=prod'
```

---

## Usage Guide

### Dashboard Overview

The Kanso dashboard is a single-page application with the following sections:

1. **Transaction Input Form** — Add manual transactions or upload CSV files
2. **Analytics Summary** — View income, expenses, and net flow
3. **Monthly Trends Chart** — Visualize spending patterns over time
4. **Transaction List** — View all transactions with categories
5. **Category Rules Panel** — Manage auto-categorization rules
6. **Unusual Transactions** — View statistical outliers

### Adding a Manual Transaction

1. Scroll to the **"Add Transaction"** form at the top
2. Enter the following fields:
   - **Date:** Select a date or type in `yyyy-MM-dd` format
   - **Description:** Brief description (e.g., "Walmart Grocery", "Salary Payment")
   - **Amount:** Use positive numbers for income, negative for expenses
   - **Category:** Leave blank for auto-categorization, or specify manually
3. Click **"Add Transaction"**
4. The transaction will be categorized automatically based on the rule engine
5. The dashboard will refresh with updated analytics

### Uploading a CSV File

#### CSV File Format

Your CSV file must have the following headers:

```
date,description,amount,category
```

#### Example CSV File

```csv
date,description,amount,category
2026-02-01,Salary,4200.00,Income
2026-02-02,Walmart Grocery,-78.31,
2026-02-03,Uber Transport,-25.50,
2026-02-04,Netflix Subscription,-15.99,Subscriptions
2026-02-05,Restaurant Dinner,-45.20,
```

#### Uploading Steps

1. Click the **"Upload CSV"** button on the dashboard
2. Select your CSV file (max 5MB)
3. Click **"Upload"**
4. The application will:
   - Parse all rows
   - Create transaction records in the database
   - Auto-categorize based on rules
   - Display the results

#### Date Format Support

The CSV importer handles multiple date formats automatically:
- `yyyy-MM-dd` (e.g., 2026-02-01)
- `MM/dd/yyyy` (e.g., 02/01/2026)
- `dd-MM-yyyy` (e.g., 01-02-2026)

#### Amount Convention

- **Positive amounts** = Income
- **Negative amounts** = Expenses
- The category field is optional; auto-categorization will run if blank

### Managing Transactions

#### Edit a Transaction

1. Click the **"Edit"** button next to any transaction in the list
2. Modify the date, description, amount, or category
3. Click **"Save"** to confirm changes

#### Delete a Transaction

1. Click the **"Delete"** button next to any transaction
2. Confirm the deletion in the modal dialog
3. The transaction is removed, analytics update automatically

#### Undo Last Action

1. Click **"Undo"** button (appears after add/import operations)
2. The last operation is reversed
3. Note: Undo only works for the most recent action

### Creating Custom Categorization Rules

#### Add a New Rule

1. Scroll to the **"Manage Rules"** section
2. Fill in the rule form:
   - **Pattern Type:** Choose `KEYWORD` (substring match) or `REGEX` (Java regex)
   - **Pattern:** Enter the pattern (e.g., "Whole Foods", "^Salary.*")
   - **Category:** Choose the category (e.g., "Groceries", "Income")
   - **Priority:** Higher numbers win (default: 0; built-in rules: 5-20)
   - **Enabled:** Toggle to enable/disable the rule
3. Click **"Add Rule"**
4. The rule is active immediately for new or edited transactions

#### Rule Priority System

Rules are resolved using a two-level priority system:

1. **Primary Sort:** Higher priority number wins
2. **Tiebreaker:** Longest pattern wins

Examples:
- Pattern "Whole Foods" wins over "Foods" if both match
- Custom rule with priority 15 beats default rule with priority 10

#### Rule Types

- **KEYWORD:** Simple substring match (case-insensitive)
  - Example: Pattern "walmart" matches "Walmart Supermarket"
- **REGEX:** Full Java regex support
  - Example: Pattern `^Salary\s*\(.*\)$` matches "Salary (Monthly, 2026-02)"

### Viewing Analytics

#### Dashboard Summary

The top of the dashboard displays:
- **Total Transactions:** Count of all transactions
- **Total Income:** Sum of positive amounts
- **Total Expenses:** Sum of negative amounts
- **Net Flow:** Income - Expenses

#### Monthly Trends

A table shows month-by-month analysis:
- **Month:** The month/year
- **Income:** Total income for that month
- **Expenses:** Total expenses for that month
- **Net:** Income - Expenses for that month
- **MoM Change:** Percentage change from previous month
- **3-Month Avg:** Rolling 3-month average

#### Top Spending Categories

A section displays the top 5 categories by expense amount:
- Category name
- Total amount spent
- Percentage of total expenses
- Trend indicator (up/down)

#### Anomaly Detection

The **"Unusual Transactions"** section highlights expenses that exceed:

```
mean(all_expense_amounts) + 2 * std_dev(all_expense_amounts)
```

- Click an anomaly to view details
- Learn why it was flagged
- Contextual information (e.g., "2.5σ above mean")

### Recurring Expenses

Kanso automatically detects recurring transactions:

1. Transactions are grouped by normalized description
2. Amounts must be within 5% tolerance
3. Recurring patterns help identify subscriptions and regular bills
4. View in the **"Recurring Expenses"** section

### Exporting Data

#### Export as CSV

1. Click **"Export Transactions"** button
2. (Optional) Filter by date range
3. Click **"Download CSV"**
4. File is downloaded as `transactions-export-[timestamp].csv`

#### Exported File Format

```csv
date,description,amount,category,matched_rule_id
2026-02-01,Salary,4200.00,Income,16
2026-02-02,Walmart Grocery,-78.31,Groceries,1
```

---

## Technical Architecture

### Layered Architecture Diagram

```
┌──────────────────────────────────────────────────────────────┐
│           Thymeleaf Template (dashboard.html)                │
│                  + CSS Stylesheet                            │
└──────────────────────────────────────────┬───────────────────┘
                                           │
┌──────────────────────────────────────────▼───────────────────┐
│         DashboardController                                  │
│   (HTTP routing, model binding, view selection)              │
└──────────────────────────────────────────┬───────────────────┘
                                           │
        ┌──────────────────────────────────┼──────────────────────────┐
        │                                  │                          │
┌───────▼──────────────────┐  ┌────────────▼──────────────┐  ┌─────────▼──────────┐
│ FinanceTrackerService    │  │ RuleEngineService        │  │ CsvImportService  │
│ (orchestration, queries) │  │ (categorization logic)   │  │ (CSV parsing)     │
│                          │  │                          │  │                   │
│ - getAnalyticsSummary()  │  │ - categorizeTransaction()│  │ - parseCSV()      │
│ - getMonthlyTrends()     │  │ - addRule()              │  │ - detectFormat()  │
│ - getAnomalies()         │  │ - updateRule()           │  │ - parseDate()     │
│ - getRecurring()         │  │ - resolveConflicts()     │  │ - validate()      │
│ - getTopCategories()     │  │                          │  │                   │
└───────┬──────────────────┘  └────────────┬──────────────┘  └─────────┬─────────┘
        │                                  │                          │
        └──────────────────────────────────┼──────────────────────────┘
                                           │
        ┌──────────────────────────────────┼──────────────────────────┐
        │                                  │                          │
┌───────▼──────────────────┐  ┌────────────▼──────────────┐  ┌─────────▼──────────┐
│ TransactionRepository    │  │ CategoryRuleRepository   │  │ StorageService    │
│ (JPA queries)            │  │                          │  │ (file I/O)        │
│                          │  │ - findByCategory()       │  │ - saveUpload()    │
│ - findByDateBetween()    │  │ - findByPattern()        │  │ - deleteUpload()  │
│ - findByCategory()       │  │ - findByPriority()       │  │ - listUploads()   │
└──────────┬───────────────┘  └────────────┬──────────────┘  └─────────┬─────────┘
           │                               │                          │
           └───────────────────────────────┼──────────────────────────┘
                                           │
┌──────────────────────────────────────────▼───────────────────┐
│         JPA / Hibernate + Flyway Migrations                  │
│          (ORM layer, schema versioning)                      │
└──────────────────────────────────────────┬───────────────────┘
                                           │
┌──────────────────────────────────────────▼───────────────────┐
│    H2 Embedded Database (./data/kanso-db)                    │
│         (Local file-based SQL database)                      │
└──────────────────────────────────────────────────────────────┘
```

### Design Principles

1. **Separation of Concerns:** Each service has a single responsibility
2. **DTO Pattern:** Entities never leak beyond the repository layer
3. **Determinism:** Rule engine logic is pure (no random state)
4. **Explainability:** Every decision is logged with justification
5. **Transactionality:** Database changes are ACID-compliant via JPA transactions
6. **Offline-First:** No dependencies on external APIs or services

### Service Layer Overview

#### FinanceTrackerService
Orchestrates high-level financial operations:
- Retrieves analytics (income, expenses, trends)
- Detects anomalies using statistical methods
- Identifies recurring transactions
- Manages transaction CRUD operations

#### RuleEngineService
Implements deterministic categorization logic:
- Matches transaction descriptions against rules
- Resolves conflicts using priority + length tiebreaker
- Logs categorization decisions for explainability
- Manages rule CRUD with enable/disable support

#### CsvImportService
Parses and validates CSV files:
- Detects date format automatically
- Validates CSV structure (headers, data types)
- Converts rows to TransactionEntity objects
- Supports bulk import with exception handling

#### StorageService
Handles file I/O operations:
- Saves uploaded CSV files to disk
- Maps filenames with secure path handling (prevents directory traversal)
- Tracks uploads in the database for audit trails
- Optional AES-256-GCM encryption for sensitive uploads

---

## Database Schema

### Overview

Kanso uses H2 database (file-based) with 4 core tables and automatic schema versioning via Flyway.

### Schema Diagram

```
┌──────────────────────────────┐
│        profile               │
├──────────────────────────────┤
│ id (PK)                      │
│ display_name: VARCHAR(100)   │
│ encryption_salt: VARCHAR(64) │
│ created_at: TIMESTAMP        │
│ updated_at: TIMESTAMP        │
└──────────────────────────────┘
           │
           │ (1:N)
           │
┌──────────────────────────────────────┐
│      category_rule                   │
├──────────────────────────────────────┤
│ id (PK)                              │
│ pattern_type: VARCHAR(10)            │
│   → 'KEYWORD' | 'REGEX'              │
│ pattern: VARCHAR(255)                │
│ category: VARCHAR(100)               │
│ priority: INT                        │
│ enabled: BOOLEAN                     │
│ is_default: BOOLEAN                  │
│ created_at: TIMESTAMP                │
└──────────────────────────────────────┘
           ▲
           │ (FK: matched_rule_id)
           │
┌──────────────────────────────────────┐
│      transaction                     │
├──────────────────────────────────────┤
│ id (PK)                              │
│ date: DATE                           │
│ description: VARCHAR(500)            │
│ amount: DECIMAL(15,2)                │
│ category: VARCHAR(100)               │
│ matched_rule_id: BIGINT (FK, NULL)   │
│ created_at: TIMESTAMP                │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│      uploaded_file                   │
├──────────────────────────────────────┤
│ id (PK)                              │
│ original_name: VARCHAR(255)          │
│ stored_path: VARCHAR(500)            │
│ encrypted: BOOLEAN                   │
│ row_count: INT                       │
│ uploaded_at: TIMESTAMP               │
└──────────────────────────────────────┘
```

### Table Definitions

#### profile

Stores single-user settings and encryption configuration.

```sql
CREATE TABLE profile (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_name     VARCHAR(100) NOT NULL DEFAULT 'Default User',
    encryption_salt  VARCHAR(64),
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Notes:**
- Single row (single-user app)
- `encryption_salt`: Hex-encoded salt for PBKDF2 key derivation (NULL if encrypted not configured)
- Seeded with default user on initialization

#### category_rule

Stores categorization rules with pattern matching and priority resolution.

```sql
CREATE TABLE category_rule (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    pattern_type VARCHAR(10) NOT NULL DEFAULT 'KEYWORD',
    pattern      VARCHAR(255) NOT NULL,
    category     VARCHAR(100) NOT NULL,
    priority     INT NOT NULL DEFAULT 0,
    enabled      BOOLEAN NOT NULL DEFAULT TRUE,
    is_default   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Columns:**
- `pattern_type`: `KEYWORD` (substring) or `REGEX` (Java regex)
- `pattern`: The pattern to match (case-insensitive for KEYWORD)
- `category`: Category to assign if matched
- `priority`: Resolution priority (higher wins; on tie, longest pattern wins)
- `enabled`: Soft-delete flag (disabled rules still exist in DB)
- `is_default`: Whether this is a seeded default rule
- **Index:** None (small cardinality, frequent full scans acceptable)

**Seeded Default Rules:** 20 rules covering Groceries, Transport, Fuel, Utilities, Subscriptions, Housing, Income, Shopping, Dining

#### transaction

Core financial record table.

```sql
CREATE TABLE transaction (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    date            DATE NOT NULL,
    description     VARCHAR(500) NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    category        VARCHAR(100) NOT NULL DEFAULT 'Uncategorized',
    matched_rule_id BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_rule FOREIGN KEY (matched_rule_id) 
        REFERENCES category_rule(id) ON DELETE SET NULL
);

CREATE INDEX idx_transaction_date     ON transaction(date);
CREATE INDEX idx_transaction_category ON transaction(category);
```

**Columns:**
- `date`: Transaction date (for monthly trend queries)
- `description`: User-provided or bank description
- `amount`: Positive (income) or negative (expense)
- `category`: Assigned category (result of rule match)
- `matched_rule_id`: Foreign key to the rule that matched (audit trail)
- **Indexes:** date (for trend queries), category (for analytics)

#### uploaded_file

Audit trail for CSV uploads.

```sql
CREATE TABLE uploaded_file (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name  VARCHAR(255) NOT NULL,
    stored_path    VARCHAR(500) NOT NULL,
    encrypted      BOOLEAN NOT NULL DEFAULT FALSE,
    row_count      INT NOT NULL DEFAULT 0,
    uploaded_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Notes:**
- `stored_path`: Secure filename mapping (prevents traversal attacks)
- `row_count`: Number of transactions imported from this file
- `encrypted`: Whether the file was encrypted before storage

### Flyway Migrations

Schema changes are version-controlled using Flyway:

- **V1__init.sql** — Initial schema with all 4 tables + 20 default rules
- **V2__budget_audit_tables.sql** — Future: Audit/logging enhancements (reserved)

Migrations are applied automatically on application startup in order. Never edit past migrations; create new migrations for schema changes.

---

## API Endpoints & Controllers

### Single-Page Application Design

Kanso is **server-side rendered** using Thymeleaf. There is **one main controller** (`DashboardController`) that handles all requests.

### DashboardController

Located in [src/main/java/com/bankingoop/finance/controller/DashboardController.java](src/main/java/com/bankingoop/finance/controller/DashboardController.java)

#### Endpoints

| HTTP Method | URL | Handler | Purpose |
|---|---|---|---|
| GET | `/` | `dashboard()` | Render main dashboard page |
| POST | `/transaction/add` | `addTransaction()` | Create new transaction |
| POST | `/transaction/{id}/edit` | `editTransaction()` | Update existing transaction |
| POST | `/transaction/{id}/delete` | `deleteTransaction()` | Delete transaction |
| POST | `/transaction/undo` | `undoLastAction()` | Undo last add/import |
| POST | `/upload/csv` | `uploadCsv()` | Upload and import CSV file |
| POST | `/rule/add` | `addRule()` | Create new categorization rule |
| POST | `/rule/{id}/edit` | `editRule()` | Update rule |
| POST | `/rule/{id}/delete` | `deleteRule()` | Delete rule |
| POST | `/export/csv` | `exportTransactions()` | Download transactions as CSV |
| GET | `/h2-console*` | H2 Web UI | Database console (dev only, localhost:8080/h2-console) |

#### Sample Request/Response (Add Transaction)

**Request:**
```http
POST /transaction/add HTTP/1.1
Content-Type: application/x-www-form-urlencoded

date=2026-03-09&description=Walmart+Grocery&amount=-75.43&category=
```

**Response:**
```html
<!-- Dashboard page rendered with new transaction in list -->
```

#### Form Data Parameters

**Add Transaction:**
- `date`: Date in format `yyyy-MM-dd`
- `description`: Transaction description (string)
- `amount`: Numeric amount (decimal or integer)
- `category`: Optional; auto-categorized if blank

**Upload CSV:**
- `file`: CSV file (multipart/form-data)

**Add Rule:**
- `patternType`: `KEYWORD` or `REGEX`
- `pattern`: Pattern string
- `category`: Category name
- `priority`: Integer (0-100)
- `enabled`: Boolean (checkbox)

### Backend API / OpenAPI Documentation

The application includes **SpringDoc OpenAPI** for REST API documentation:

```
http://localhost:8080/swagger-ui.html
```

This provides:
- Swagger UI for interactive API exploration
- OpenAPI 3.0 specification
- Request/response schema definitions
- Try-it-out functionality (if exposed)

---

## Rule Engine & Categorization

### How Categorization Works

When a new transaction is added or updated, the **RuleEngineService** attempts to categorize it automatically:

1. **Extract Keywords:** Transaction description is lowercased
2. **Find Matching Rules:** All enabled rules are evaluated
3. **Resolve Conflicts:** If multiple rules match, apply priority tiebreaker
4. **Assign Category:** Winning rule's category is assigned to transaction
5. **Log Decision:** Categorization is logged at DEBUG level with rule ID

### Rule Matching Algorithm

```java
// Pseudocode for rule resolution
List<MatchedRule> matches = new ArrayList<>();

for (CategoryRule rule : enabledRules) {
    if (rule.matches(transactionDescription)) {
        matches.add(rule);  // Add match with pattern length
    }
}

// Sort by: (1) priority DESC, (2) pattern length DESC
matches.sort(
    Comparator.comparingInt(MatchedRule::getPriority).reversed()
        .thenComparingInt(MatchedRule::getPatternLength).reversed()
);

// Return highest-ranked rule's category
return matches.isEmpty() 
    ? "Uncategorized" 
    : matches.get(0).getCategory();
```

### Rule Types

#### KEYWORD Rules

Case-insensitive substring matching:

```sql
INSERT INTO category_rule (pattern_type, pattern, category, priority)
VALUES ('KEYWORD', 'walmart', 'Groceries', 10);
```

Example matches:
- "Walmart Supermarket" ✓
- "WALMART STORE #1234" ✓
- "Target" ✗

#### REGEX Rules

Full Java regex pattern matching:

```sql
INSERT INTO category_rule (pattern_type, pattern, category, priority)
VALUES ('REGEX', '^(Salary|Payroll|Bonus).*', 'Income', 20);
```

Example matches:
- "Salary Deposit" ✓
- "Payroll Transfer" ✓
- "Bonus Payment - Q1 2026" ✓
- "Salary Review Letter" ✗ (no explicit match)

### Priority Resolution

Rules are evaluated in this order:

1. **Primary:** Higher `priority` value wins
2. **Tiebreaker:** Longer pattern length wins (more specific)

Example:

```sql
-- Rule A: priority=10, pattern="walmart" (length 7)
INSERT INTO category_rule VALUES (1, 'KEYWORD', 'walmart', 'Groceries', 10, TRUE, FALSE);

-- Rule B: priority=10, pattern="walmart grocery" (length 14)
INSERT INTO category_rule VALUES (2, 'KEYWORD', 'walmart grocery', 'Groceries', 10, TRUE, FALSE);
```

For transaction "Walmart Grocery Store #5":
- Rule A matches (length 7)
- Rule B matches (length 14)
- **Rule B wins** (same priority, longer pattern)

### Special Cases

- **No matches:** Category defaults to `"Uncategorized"`
- **Disabled rules:** Rules with `enabled=FALSE` are skipped
- **Null matched_rule_id:** Transactions can have `NULL` matched_rule_id if manually assigned

### Custom Rules Best Practices

1. **Keep patterns specific:** Longer patterns score higher in tiebreaks
2. **Use KEYWORD for simple cases:** Cleaner and more performant than REGEX
3. **Set appropriate priority:** Default rules start at 5-20; custom rules often need 15+ to override
4. **Document intent:** Add comments in rule description field explaining the pattern
5. **Test before deployment:** Add a rule, then verify it categorizes a transaction correctly

---

## Analytics & Anomaly Detection

### Analytics Summary

The dashboard displays:

| Metric | Calculation |
|--------|-------------|
| **Total Transactions** | COUNT(*) |
| **Total Income** | SUM(amount) WHERE amount > 0 |
| **Total Expenses** | SUM(ABS(amount)) WHERE amount < 0 |
| **Net Flow** | Total Income - Total Expenses |
| **Savings Rate** | (Income - Expenses) / Income * 100% |

### Monthly Trends

Trends are calculated per calendar month:

```
FOR each month in [first_transaction_month .. today]:
    income_month   = SUM(amount) WHERE month = $month AND amount > 0
    expenses_month = SUM(ABS(amount)) WHERE month = $month AND amount < 0
    net_month      = income_month - expenses_month
    
    mom_change = (net_month - net_previous_month) / net_previous_month * 100%
    
    rolling_avg_3m = AVG(net_month, net_month-1, net_month-2)
```

### Anomaly Detection (Statistical Outliers)

Unusual expenses are flagged using the **σ threshold method**:

```
outlier_threshold = mean(all_expenses) + 2 * stddev(all_expenses)

For each expense:
    IF expense > outlier_threshold:
        Flag as "Unusual"
        Calculate z_score = (expense - mean) / stddev
        Display reason: "z-score: {z_score}, {x} standard deviations above mean"
```

**Why 2σ?**
- Approximately 95% of transactions fall within 2σ of the mean (normal distribution)
- Only ~5% are potentially anomalous
- Good balance between sensitivity and false positives

**Example:**

```
Expenses: [50, 55, 48, 52, 600, 49, 51]
        Mean = 150.71
        StdDev = 178.34
        Threshold = 150.71 + 2 * 178.34 = 507.39
        
        → Expense of 600 is flagged (600 > 507.39)
        → z-score = (600 - 150.71) / 178.34 = 2.52σ
```

### Top Spending Categories

Aggregates and ranks categories by total expense:

```sql
SELECT category, SUM(ABS(amount)) as total_spent
FROM transaction
WHERE amount < 0
GROUP BY category
ORDER BY total_spent DESC
LIMIT 5;
```

### Recurring Transactions

Identifies transactions that repeat with similar amounts:

1. **Normalize descriptions:** Remove amounts, dates, timestamps
2. **Group by normalized description:** Transactions with same description are grouped
3. **Filter by tolerance:** Only include groups with 5% amount variance
4. **Sort by frequency:** Most recurring first

Example:

```
Input transactions:
  2026-02-01: "Netflix Subscription" -15.99
  2026-03-01: "Netflix Subscription" -15.99
  2026-03-15: "Coffee" -5.50
  2026-03-20: "Coffee" -5.45

Output:
  Netflix Subscription: 2 occurrences, -15.99 avg
  Coffee: 2 occurrences, -5.48 avg
```

---

## Security & Encryption

### Threat Model

**Threats Mitigated:**

1. **Unauthorized remote access**
   - Mitigation: Server binds to `127.0.0.1` only (localhost)
   - Only local users can access the application

2. **CSV path traversaler attacks**
   - Mitigation: Secure filename mapping (random UUID + hash)
   - Prevents uploads like `../../../etc/passwd`

3. **Sensitive file exposure**
   - Mitigation: Optional AES-256-GCM encryption for uploaded files
   - Encryption key derived from passphraseusing PBKDF2

4. **SQL injection**
   - Mitigation: JPA parameterized queries (no string concatenation)
   - Hibernate translates object queries to parameterized SQL

5. **CSV injection (formula injection)**
   - Noted but accepted risk for local single-user app
   - Future: Escape formulas in export if needed

### Encryption (Optional)

Kanso supports optional AES-256-GCM encryption for CSV uploads:

#### Enable Encryption

1. Set an encryption passphrase (stored in `profile.encryption_salt`)
2. Toggle "Enable Encryption" in dashboard (if feature is implemented)
3. Uploaded files are encrypted before storage

#### Encryption Algorithm

- **Cipher:** AES-256 in GCM mode (authenticated encryption)
- **Key Derivation:** PBKDF2WithHmacSHA256, 600,000 iterations
- **IV Size:** 96 bits (GCM recommended)
- **Tag Size:** 128 bits (authentication tag)

#### Encryption Flow

```
Passphrase (user input)
    ↓
PBKDF2WithHmacSHA256 (600k iterations, server salt)
    ↓
256-bit key
    ↓
AES-256-GCM encrypt
    ↓
Ciphertext + authentication tag + IV
    ↓
Stored in ./data/uploads/
```

### Authentication & Authorization

- **No authentication:** Single-user local app
- **No authorization:** All actions allowed to local user
- **Future:** Support multiple profiles with encrypted separation

### Database Security

- **H2 credentials:** Default `sa` user with no password (OK for local file DB)
- **No remote access:** H2 only accepts local connections
- **No backups:** Data persists in local file only (user must backup `./data/` folder)

### Best Practices for Users

1. **Backup regularly:** Copy `./data/` folder to external storage
2. **Keep passphrases secure:** Encryption passphrase should be strong if enabled
3. **Restrict file access:** Limit access to `./data/kanso-db` file (contains all financial data)
4. **Use HTTPS for remote access:** If deploying to cloud, always use TLS
5. **Rotate encryption keys:** Implement key rotation if deployed at scale

---

## Project Structure

### Directory Tree

```
BankingOOP/
├── pom.xml                                      # Maven build config
├── Dockerfile                                   # Container image definition
├── cloudbuild.yaml                              # Google Cloud Build pipeline
├── deploy-to-cloud-run.sh                       # Cloud Run deployment script
├── CLOUD_RUN_DEPLOYMENT.md                      # Cloud deployment guide
├── README.md                                    # This file
│
├── src/
│   ├── main/
│   │   ├── java/com/bankingoop/finance/
│   │   │   ├── PersonalFinanceTrackerApplication.java              # Entry point
│   │   │   │
│   │   │   ├── config/
│   │   │   │   └── StorageConfig.java                             # Directory initialization
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   └── DashboardController.java                       # Main @ Controller
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── AnalyticsSummaryDto.java                      # Analytics DTO
│   │   │   │   ├── CategoryRuleDto.java                          # Rule DTO
│   │   │   │   ├── CategorySpendDto.java                         # Spending DTO
│   │   │   │   ├── MonthlyTrendDto.java                          # Trend DTO
│   │   │   │   ├── RecurringTransactionDto.java                  # Recurring DTO
│   │   │   │   ├── RuleSuggestionDto.java                        # Suggestion DTO
│   │   │   │   ├── TransactionDto.java                           # Transaction DTO
│   │   │   │   └── UnusualTransactionDto.java                    # Anomaly DTO
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── CategoryRuleEntity.java                       # Rule entity
│   │   │   │   ├── ProfileEntity.java                            # Profile entity
│   │   │   │   ├── TransactionEntity.java                        # Transaction entity
│   │   │   │   └── UploadedFileEntity.java                       # Upload entity
│   │   │   │
│   │   │   ├── event/
│   │   │   │   └── TransactionImportedEvent.java                 # Spring event (reserved)
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── InvalidCsvFormatException.java               # CSV parse error
│   │   │   │   ├── RuleConflictException.java                   # Rule conflict error
│   │   │   │   └── StorageException.java                        # File I/O error
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── MatchedRule.java                             # Rule match result
│   │   │   │   ├── RuleType.java                                # KEYWORD | REGEX enum
│   │   │   │   └── CategorizationResult.java                    # Categorization output
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── CategoryRuleRepository.java                  # JPA repository for rules
│   │   │   │   ├── ProfileRepository.java                       # JPA repository for profile
│   │   │   │   ├── TransactionRepository.java                   # JPA repository for transactions
│   │   │   │   └── UploadedFileRepository.java                  # JPA repository for uploads
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── FinanceTrackerService.java                   # Analytics & orchestration
│   │   │   │   ├── RuleEngineService.java                       # Categorization logic
│   │   │   │   ├── CsvImportService.java                        # CSV parsing
│   │   │   │   ├── StorageService.java                          # File I/O
│   │   │   │   └── EncryptionService.java                       # AES-256-GCM (optional)
│   │   │   │
│   │   │   └── util/
│   │   │       ├── DateFormatDetector.java                      # Multi-format date parsing
│   │   │       └── CsvValidator.java                            # CSV structure validation
│   │   │
│   │   └── resources/
│   │       ├── application.properties                           # Main config
│   │       ├── sample-transactions.csv                          # Sample data for testing
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__init.sql                            # Initial schema
│   │       │       └── V2__budget_audit_tables.sql             # Future migrations
│   │       ├── static/
│   │       │   └── styles.css                                  # Dashboard CSS
│   │       └── templates/
│   │           └── dashboard.html                              # Single-page template
│   │
│   └── test/
│       ├── java/com/bankingoop/finance/
│       │   ├── controller/
│       │   │   └── DashboardControllerIntegrationTest.java    # Controller tests
│       │   ├── service/
│       │   │   ├── RuleEngineServiceTest.java                 # Rule logic tests
│       │   │   ├── FinanceTrackerServiceTest.java             # Analytics tests
│       │   │   └── BudgetServiceTest.java                     # Budget tests (if present)
│       │   └── ApiControllerTest.java                         # API tests
│       └── resources/
│           └── application-test.properties                     # Test configuration
│
├── data/                                        # Runtime directory (created on first run)
│   ├── kanso-db.mv.db                          # H2 database file
│   └── uploads/                                # CSV uploads
│
└── target/                                      # Build output (generated by Maven)
    ├── classes/                                 # Compiled .class files
    ├── test-classes/                           # Compiled test .class files
    └── kanso-1.0.0.jar                        # Executable JAR
```

### Key Files by Purpose

#### Entry Point
- [src/main/java/com/bankingoop/finance/PersonalFinanceTrackerApplication.java](src/main/java/com/bankingoop/finance/PersonalFinanceTrackerApplication.java)

#### Domain Logic
- [src/main/java/com/bankingoop/finance/service/RuleEngineService.java](src/main/java/com/bankingoop/finance/service/RuleEngineService.java) — Rule matching algorithm
- [src/main/java/com/bankingoop/finance/service/FinanceTrackerService.java](src/main/java/com/bankingoop/finance/service/FinanceTrackerService.java) — Analytics
- [src/main/java/com/bankingoop/finance/service/CsvImportService.java](src/main/java/com/bankingoop/finance/service/CsvImportService.java) — CSV parsing

#### Data Access
- [src/main/java/com/bankingoop/finance/repository/](src/main/java/com/bankingoop/finance/repository/) — JPA repositories

#### Web Layer
- [src/main/java/com/bankingoop/finance/controller/DashboardController.java](src/main/java/com/bankingoop/finance/controller/DashboardController.java) — HTTP endpoints
- [src/main/resources/templates/dashboard.html](src/main/resources/templates/dashboard.html) — HTML template
- [src/main/resources/static/styles.css](src/main/resources/static/styles.css) — Styling

#### Database
- [src/main/resources/db/migration/V1__init.sql](src/main/resources/db/migration/V1__init.sql) — Schema + seed data
- [src/main/resources/application.properties](src/main/resources/application.properties) — Configuration

#### Testing
- [src/test/java/](src/test/java/) — Integration and unit tests
- [src/test/resources/application-test.properties](src/test/resources/application-test.properties) — Test config

---

## Testing

### Test Structure

Tests are located in [src/test/java/com/bankingoop/finance/](src/test/java/com/bankingoop/finance/) and organized by layer:

```
src/test/
├── java/com/bankingoop/finance/
│   ├── controller/
│   │   └── DashboardControllerIntegrationTest.java
│   ├── service/
│   │   ├── RuleEngineServiceTest.java
│   │   ├── FinanceTrackerServiceTest.java
│   │   └── BudgetServiceTest.java
│   └── ApiControllerTest.java
└── resources/
    └── application-test.properties
```

### Running Tests

#### Run All Tests

```bash
mvn test
```

#### Run a Specific Test Class

```bash
mvn test -Dtest=RuleEngineServiceTest
```

#### Run a Specific Test Method

```bash
mvn test -Dtest=RuleEngineServiceTest#testRulePriorityResolution
```

#### Run Tests with Coverage Report

```bash
mvn test jacoco:report
# Report goes to target/site/jacoco/
```

#### Run Tests in IDE

- **IntelliJ IDEA:** Right-click test class → "Run" or "Debug"
- **VS Code + Extension Pack for Java:** Click "Run" above test class
- **Eclipse:** Right-click test → "Run As" → "JUnit Test"

### Test Architecture

All tests extend `AbstractIntegrationTest` which:
- Sets up an embedded H2 database for testing
- Clears tables before each test (transactional rollback)
- Provides utility methods for creating test data
- Uses `@SpringBootTest` for full application context

### Test Categories

#### Unit Tests
- Single service in isolation
- No database or network
- Fast execution

#### Integration Tests
- Full Spring context
- Real H2 database
- Slower but comprehensive

#### Sample Tests

**RuleEngineServiceTest** — Tests rule matching logic:
- Single keyword match
- Multiple rule matches (priority resolution)
- REGEX pattern matching
- Disabled rule handling
- Conflict resolution (length tiebreaker)

**FinanceTrackerServiceTest** — Tests analytics:
- Monthly trend calculation
- Anomaly detection (2σ threshold)
- Recurring transaction detection
- Top category aggregation

**DashboardControllerIntegrationTest** — Tests HTTP endpoints:
- CSV upload handling
- Manual transaction add
- Export CSV
- Rule CRUD

### Test Database Configuration

[src/test/resources/application-test.properties](src/test/resources/application-test.properties):

```properties
# In-memory H2 for fast tests (no file I/O)
spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

**Why in-memory?**
- Fast: No disk I/O
- Isolated: Each test gets fresh database
- Clean: DDL creates/drops automatically

### Test Data

Sample transactions for testing are in [src/main/resources/sample-transactions.csv](src/main/resources/sample-transactions.csv):

```csv
date,description,amount,category
2026-02-01,Salary,4200.00,Income
2026-02-02,Walmart Grocery Store,-78.31,
2026-02-03,Uber Transport Service,-25.50,
...
```

These are used for:
- Manual integration test data loading
- Demonstration of CSV format
- Multi-format date testing

---

## Building & Deployment

### Local Development Build

#### Build Without Running Tests

```bash
mvn clean package -DskipTests
```

#### Build With Tests

```bash
mvn clean package
```

This produces: `target/kanso-1.0.0.jar`

#### Build and Run Directly

```bash
mvn clean spring-boot:run
```

Starts the application on `http://localhost:8080`.

### Executable JAR

#### Package as Executable JAR

```bash
mvn clean package
```

#### Run the JAR

```bash
java -jar target/kanso-1.0.0.jar
```

#### Run JAR with Custom Properties

```bash
java -jar target/kanso-1.0.0.jar \
    --server.port=9090 \
    --kanso.storage.upload-dir=/custom/uploads/path
```

### Docker Deployment

#### Prerequisites

- Docker installed on your system
- Docker daemon running

#### Build Docker Image

```bash
docker build -t kanso:latest .
```

This:
1. Starts from official Maven image
2. Copies `pom.xml` and source
3. Builds JAR with `mvn clean package`
4. Packages JAR in lightweight Java runtime image

#### Run Docker Container

```bash
docker run -d \
    --name kanso-app \
    -p 8080:8080 \
    -v kanso-data:/app/data \
    kanso:latest
```

Then access: `http://localhost:8080`

#### Docker Compose (Manual)

```yaml
version: '3.8'
services:
  kanso:
    image: kanso:latest
    ports:
      - "8080:8080"
    volumes:
      - kanso-data:/app/data
    environment:
      SERVER_PORT: 8080
volumes:
  kanso-data:
```

Run with: `docker-compose up -d`

### Cloud Run Deployment

For Google Cloud Run deployment, see [CLOUD_RUN_DEPLOYMENT.md](CLOUD_RUN_DEPLOYMENT.md).

**Quick Summary:**
1. Authenticate: `gcloud auth login`
2. Build image: `gcloud builds submit --config=cloudbuild.yaml`
3. Deploy: `./deploy-to-cloud-run.sh`

### Production Considerations

#### Environment Variables

```bash
export SERVER_PORT=8080
export KANSO_STORAGE_UPLOAD_DIR=/data/uploads
export KANSO_STORAGE_DB_PATH=/data/kanso-db
export LOGGING_LEVEL_COM_BANKINGOOP_FINANCE=INFO

java -jar kanso-1.0.0.jar
```

#### Database Backup

Back up the database file regularly:

```bash
cp ./data/kanso-db.mv.db ./backups/kanso-db-$(date +%Y%m%d).backup
```

#### Monitoring

Health check endpoint:
```
GET http://localhost:8080/actuator/health
```

Metrics endpoint:
```
GET http://localhost:8080/actuator/metrics
```

---

## Troubleshooting

### Common Issues & Solutions

#### Issue: "Port 8080 already in use"

**Symptoms:** Error message about port 8080 when starting application

**Solutions:**
1. **Change port in application.properties:**
   ```properties
   server.port=8081
   ```
2. **Find and kill existing process:**
   ```bash
   # Windows
   netstat -ano | findstr :8080
   taskkill /PID <PID> /F
   
   # macOS/Linux
   lsof -i :8080
   kill -9 <PID>
   ```

#### Issue: "Database locked" errors

**Symptoms:** `H2Exception: Database may be locked`

**Causes:** Multiple instances running, improper shutdown

**Solutions:**
1. Ensure only one instance is running
2. Delete lock file: `rm ./data/kanso-db.lock`
3. Restart application

#### Issue: CSV upload fails with "Invalid date format"

**Symptoms:** CSV import error when uploading file

**Solutions:**
1. Check date formats in CSV — must be one of:
   - `yyyy-MM-dd` (2026-03-09)
   - `MM/dd/yyyy` (03/09/2026)
   - `dd-MM-yyyy` (09-03-2026)
2. Verify CSV headers: `date,description,amount,category`
3. Check for trailing spaces or special characters

#### Issue: Rules not matching transactions

**Symptoms:** Transactions remain "Uncategorized" despite rules existing

**Solutions:**
1. Verify rule is **enabled** (check checkbox in dashboard)
2. Check rule **pattern** is correct (case doesn't matter for KEYWORD)
3. Test rule logic:
   - For KEYWORD: Pattern must be substring of description
   - For REGEX: Pattern must match entire description
4. Increase rule **priority** if other rules are matching first

#### Issue: Anomaly detection not working

**Symptoms:** Unusual transactions not flagged

**Solutions:**
1. Need at least 3-5 expense transactions for statistical calculation
2. Anomalies are calculated from current loaded transactions only
3. Import more sample data (use sample-transactions.csv)

#### Issue: "NoSuchFileException" when uploading CSV

**Symptoms:** Upload fails with file not found error

**Solutions:**
1. Check upload directory exists: `./data/uploads/`
2. Verify file permissions (readable/writable)
3. Check file size < 5MB (configured limit)
4. Ensure file is valid CSV (not corrupted)

#### Issue: Application won't start - "Migration failed"

**Symptoms:** Flyway migration error on startup

**Solutions:**
1. Check H2 database file isn't corrupted: `rm ./data/kanso-db.mv.db`
2. Verify `db/migration/` folder exists and contains SQL files
3. Check SQL syntax in migration files
4. View Flyway logs for detailed error message

#### Issue: Slow transaction list with many records

**Symptoms:** Dashboard page loads slowly (>1000 transactions)

**Solutions:**
1. Use date filters to limit displayed transactions
2. Archive old transactions if needed
3. Add database indexes: Already done for `date` and `category`
4. Monitor with: `GET /actuator/metrics/jvm.memory.usage`

### Getting Help

#### Enable Debug Logging

Add to `application.properties`:
```properties
logging.level.com.bankingoop.finance=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

This will log:
- Rule categorization decisions
- SQL queries executed
- Parameter binding

#### Check H2 Web Console

Access: `http://localhost:8080/h2-console`

- **JDBC URL:** `jdbc:h2:file:./data/kanso-db`
- **User:** `sa`
- **Password:** (leave blank)

Run SQL queries to inspect data directly.

#### Review Application Logs

```bash
# If running with Maven
# Logs appear in terminal output

# If running JAR, redirect logs:
java -jar kanso-1.0.0.jar > kanso.log 2>&1
tail -f kanso.log
```

---

## Development Guide

### Development Setup

#### Prerequisites

- JDK 17+ (same as project)
- Maven 3.6+
- Git (for version control)
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

#### Clone & Build

```bash
git clone <repo-url>
cd BankingOOP
mvn clean install
mvn spring-boot:run
```

### Code Style & Conventions

#### Java Naming
- `TransactionEntity` — JPA entities (Pascal case)
- `transactionDto` — DTOs (camelCase)
- `transactionRepository` — service fields (camelCase)
- `UPLOAD_DIR` — constants (UPPER_SNAKE_CASE)

#### Package Organization

```
com.bankingoop.finance
├── config         → Spring configuration beans
├── controller     → HTTP request handlers
├── dto            → Data transfer objects (records)
├── entity         → JPA entities
├── event          → Spring events (publish-subscribe)
├── exception      → Custom exceptions
├── model          → Domain objects (not JPA)
├── repository     → JPA repositories
├── service        → Business logic
└── util           → Utilities
```

#### Documentation

- Use JavaDoc for public classes/methods
- Add "interview talking point" comments for design decisions
- Log important business logic at DEBUG level

### Adding a New Feature

#### Example: Add "Tags" Feature

1. **Create Entity:**
   ```java
   @Entity
   public class TagEntity {
       @Id private Long id;
       @Column private String title;
       @ManyToMany private Set<TransactionEntity> transactions;
   }
   ```

2. **Create Repository:**
   ```java
   @Repository
   public interface TagRepository extends JpaRepository<TagEntity, Long> {
       List<TagEntity> findByTitle(String title);
   }
   ```

3. **Create DTO:**
   ```java
   public record TagDto(Long id, String title, int transactionCount) {}
   ```

4. **Create Migration:**
   ```sql
   -- V3__add_tags.sql
   CREATE TABLE tag (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       title VARCHAR(100) NOT NULL UNIQUE,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   
   CREATE TABLE transaction_tags (
       transaction_id BIGINT,
       tag_id BIGINT,
       FOREIGN KEY (transaction_id) REFERENCES transaction(id),
       FOREIGN KEY (tag_id) REFERENCES tag(id),
       PRIMARY KEY (transaction_id, tag_id)
   );
   ```

5. **Add Service Logic:**
   ```java
   @Service
   public class TagService {
       public void addTagToTransaction(Long txnId, Long tagId) { ... }
       public List<TagDto> getTagsForTransaction(Long txnId) { ... }
   }
   ```

6. **Add Controller Endpoint:**
   ```java
   @PostMapping("/transaction/{id}/tag/{tagId}")
   public String addTag(@PathVariable Long id, @PathVariable Long tagId) { ... }
   ```

7. **Update Template**
   Add UI controls in `dashboard.html`

8. **Write Tests**
   Add test cases in `src/test/`

9. **Update Documentation**
   Update this README with the new feature

### Common Development Tasks

#### Rebuild After Code Changes

```bash
mvn clean compile
mvn spring-boot:run
```

Changes take effect on next server restart.

#### Format Code

```bash
mvn spotless:apply  # If spotless-maven-plugin is added
```

Or use IDE's formatter: Ctrl+Alt+L (IntelliJ), Ctrl+Shift+F (Eclipse)

#### Run Specific Test

```bash
mvn test -Dtest=RuleEngineServiceTest#testPriorityResolution
```

#### Debug Application

1. Set breakpoint in IDE
2. Run: `mvn spring-boot:run` (or debug mode in IDE)
3. Trigger the code path
4. IDE will pause at breakpoint

#### Profile Application (Performance)

```bash
# Use JProfiler, YourKit, or JFR (built-in):
java -XX:StartFlightRecording=duration=60s,filename=recording.jfr -jar kanso.jar
```

### Git Workflow

```bash
# Create feature branch
git checkout -b feature/add-tags

# Make changes, test, commit
git add .
git commit -m "feat: add tags feature to transactions"

# Push to remote
git push origin feature/add-tags

# Create pull request on GitHub
# After review and approval, merge to main
```

### Dependency Management

#### Add a New Dependency

1. Edit `pom.xml` and add to `<dependencies>`
2. Run `mvn dependency:resolve` to verify
3. Commit `pom.xml` changes
4. Let Maven download on next build

#### Example: Add Apache Commons CSV

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.10.0</version>
</dependency>
```

#### Check Dependency Tree

```bash
mvn dependency:tree
```

---

## Appendix

### Sample Transactions CSV

[src/main/resources/sample-transactions.csv](src/main/resources/sample-transactions.csv)

A CSV file with 29 sample transactions spanning 2 months, useful for:
- Testing CSV import
- Demonstrating anomaly detection
- Teaching multi-format date support

### Glossary

| Term | Definition |
|------|-----------|
| **Rule** | A categorization rule with pattern + category |
| **Anomaly** | Transaction flagged as unusual by σ threshold |
| **Recurring** | Transaction that repeats monthly (same description, ±5% amount) |
| **MoM Delta** | Month-over-month percentage change |
| **σ (Sigma)** | Standard deviation statistic |
| **KEYWORD** | Rule type using substring matching |
| **REGEX** | Rule type using Java regex patterns |
| **DTO** | Data transfer object (immutable record) |
| **Flyway** | Database migration tool |
| **JPA** | Java Persistence API for ORM |
| **H2** | Embedded SQL database |

### References

- **Spring Boot 3 Documentation:** https://spring.io/projects/spring-boot
- **H2 Database:** https://www.h2database.com/
- **Flyway Migrations:** https://flywaydb.org/documentation/
- **Java 17 Features:** https://openjdk.java.net/projects/jdk/17/
- **Thymeleaf Template Engine:** https://www.thymeleaf.org/

### License

Proprietary — All rights reserved

---

**Last Updated:** March 9, 2026  
**Author:** Sai Uttej R  
**Version:** 2.0.0

## Deployment

### Google Cloud Run (Recommended - Free Tier)

Deploy Kanso to Google Cloud Run with automatic scaling and a generous free tier (2M requests/month).

**Quick Start:**
```bash
./deploy-to-cloud-run.sh
```

Or follow the detailed guide: [CLOUD_RUN_DEPLOYMENT.md](CLOUD_RUN_DEPLOYMENT.md)

**Benefits:**
- ✅ Completely free for typical usage
- ✅ Auto-scales with traffic
- ✅ CI/CD integration with Cloud Build
- ✅ 24/7 uptime with no sleep periods
- ✅ Easy custom domain setup

**Estimated Cost:** $0-5/month for typical usage

For detailed instructions, see: [CLOUD_RUN_DEPLOYMENT.md](CLOUD_RUN_DEPLOYMENT.md)

## Suggested Next Iterations

1. Persist data/rules to PostgreSQL.
2. Add user authentication.
3. Add charts with a JS library (Chart.js/ECharts).
4. Add recurring transaction detection.
