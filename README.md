# Kanso - By Sai Uttej R

A Java Spring Boot web app for tracking personal transactions with:
- CSV upload/import
- manual transaction entry
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

## Suggested Next Iterations

1. Persist data/rules to PostgreSQL.
2. Add user authentication.
3. Add charts with a JS library (Chart.js/ECharts).
4. Add recurring transaction detection.
