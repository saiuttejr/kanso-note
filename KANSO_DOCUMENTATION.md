# Kanso — Personal Finance Tracker

### Comprehensive Technical Documentation

**Author:** Sai Uttej R  
**Version:** 2.0.0  
**Tech Stack:** Spring Boot 3.4.2 · Java 17 · H2 (file-mode) · Thymeleaf · Flyway · AES-256-GCM  
**Repository:** `kanso-note-main`

---

## Table of Contents

1. [Developer Intent & Motivation](#1-developer-intent--motivation)
2. [Project Overview](#2-project-overview)
3. [Architecture & Design Philosophy](#3-architecture--design-philosophy)
4. [Technology Stack & Rationale](#4-technology-stack--rationale)
5. [Project Structure](#5-project-structure)
6. [Database Schema & Migrations](#6-database-schema--migrations)
7. [Configuration & Profiles](#7-configuration--profiles)
8. [Domain Model — Entities](#8-domain-model--entities)
9. [Repository Layer](#9-repository-layer)
10. [DTO Layer — Data Transfer Objects](#10-dto-layer--data-transfer-objects)
11. [Service Layer](#11-service-layer)
    - [Rule Engine Service](#111-rule-engine-service)
    - [Finance Tracker Service](#112-finance-tracker-service)
    - [CSV Import Service](#113-csv-import-service)
    - [Storage Service](#114-storage-service)
12. [Controller Layer](#12-controller-layer)
13. [Security & Encryption](#13-security--encryption)
14. [Frontend — Thymeleaf Templates & CSS](#14-frontend--thymeleaf-templates--css)
15. [Feature Catalogue](#15-feature-catalogue)
16. [Algorithms & Technical Deep-Dives](#16-algorithms--technical-deep-dives)
17. [Testing Strategy](#17-testing-strategy)
18. [Build, Run & Deploy](#18-build-run--deploy)
19. [Sample Data](#19-sample-data)
20. [API / Endpoint Reference](#20-api--endpoint-reference)
21. [Design Decisions & Trade-offs](#21-design-decisions--trade-offs)
22. [Future Roadmap](#22-future-roadmap)

---

## 1. Developer Intent & Motivation

### Why Kanso Exists

Kanso was built as a **deliberate, end-to-end demonstration of software engineering skills** — not just a CRUD app, but a thoughtfully designed system that showcases:

- **Object-Oriented Design** applied to a real-world financial domain
- **Offline-first architecture** where every design choice enforces zero network dependency
- **Deterministic algorithms** (rule engine with priority + longest-match conflict resolution) that are explainable and testable
- **Statistical analysis** (anomaly detection, recurring pattern detection, trend analytics) implemented from scratch in Java — no external ML libraries
- **Security-conscious engineering** with AES-256-GCM encryption, path-traversal guards, localhost-only binding, and documented threat models
- **Production-quality patterns** — Flyway migrations, JPA with validate mode, DTO separation, transactional boundaries, and comprehensive logging

### The Name "Kanso" (簡素)

Kanso is a Japanese aesthetic principle meaning **simplicity** and **elimination of clutter**. The project embodies this: a clean, single-page dashboard that surfaces actionable financial insights without overwhelming the user. Every feature earns its place by solving a specific problem — categorizing transactions, detecting anomalies, tracking recurring spending, or helping the user understand their savings rate.

### Personal Goals Behind the Project

1. **Demonstrate full-stack ownership** — from SQL schema design (Flyway migrations) through JPA entities, service orchestration, controller routing, to Thymeleaf templating and CSS.
2. **Show algorithmic thinking** — the rule engine's conflict resolution, the σ-based anomaly detector, and the 5%-tolerance recurring detector are all hand-rolled algorithms with clear, explainable logic.
3. **Prove security awareness** — AES-256-GCM encryption with PBKDF2 key derivation, path-traversal prevention, localhost-only binding, and separation of concern between storage and encryption.
4. **Build something actually usable** — not a toy demo, but a finance tracker that handles CSV import, inline editing, undo, export, date filtering, and onboarding for first-time users.
5. **Interview readiness** — every major design decision is annotated with "interview talking point" comments in the source code, making the codebase self-documenting for technical discussions.

---

## 2. Project Overview

Kanso is a **single-user, offline-first personal finance tracker** built with Spring Boot. It runs entirely on `localhost:8080` with an embedded H2 database stored as a local file (`./data/kanso-db`). No internet connection is ever required.

### Core Capabilities

| Capability | Description |
|---|---|
| **Transaction Management** | Add, edit, delete, and bulk-import transactions via CSV |
| **Auto-Categorization** | Deterministic rule engine with KEYWORD/REGEX patterns, priority-based conflict resolution |
| **Analytics Dashboard** | Monthly trends with MoM delta, 3-month rolling average, top spending categories, savings rate |
| **Anomaly Detection** | Statistical outlier detection using mean + 2σ threshold |
| **Recurring Detection** | Groups transactions by normalized description with 5% amount tolerance |
| **Data Security** | AES-256-GCM encryption for uploaded files and database backups |
| **Export** | CSV export with optional date range filtering |
| **Undo** | Single-step undo of the last add/import action |
| **Onboarding** | First-use wizard guiding new users through sample data loading |

### Key Numbers

- **4 database tables** (profile, category_rule, transaction, uploaded_file)
- **20 seed categorization rules** covering groceries, transport, fuel, subscriptions, housing, utilities, income, shopping, dining
- **8 DTO records** for clean data transfer between layers
- **6 integration tests** covering rule engine conflict resolution
- **5 date format parsers** in the CSV importer
- **29 sample transactions** spanning 2 months with income, expenses, and anomaly triggers
- **1 Thymeleaf page** — the entire UI is a single-page server-rendered dashboard

---

## 3. Architecture & Design Philosophy

### Layered Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Thymeleaf Template                    │
│                  (dashboard.html + CSS)                 │
├─────────────────────────────────────────────────────────┤
│                  DashboardController                    │
│            (request routing, model binding)             │
├─────────────────────────────────────────────────────────┤
│     FinanceTrackerService    │    RuleEngineService     │
│    (orchestration, analytics)│ (categorization, CRUD)   │
│                              │                          │
│     CsvImportService         │    StorageService        │
│    (parsing, date formats)   │ (file I/O, encryption)   │
├─────────────────────────────────────────────────────────┤
│  TransactionRepository  │  CategoryRuleRepository       │
│  ProfileRepository      │  UploadedFileRepository       │
├─────────────────────────────────────────────────────────┤
│          JPA / Hibernate + Flyway Migrations            │
├─────────────────────────────────────────────────────────┤
│            H2 Database (file: ./data/kanso-db)          │
└─────────────────────────────────────────────────────────┘
```

### Design Principles

1. **Offline-First** — No external API calls, no cloud services, no CDNs. Everything runs locally.
2. **Determinism** — The rule engine always produces the same output for the same input. No randomness, no ML black boxes.
3. **Explainability** — Every categorization decision is logged at DEBUG level with the winning rule ID, priority, pattern, and match length.
4. **Schema-First** — Flyway owns the database schema. Hibernate runs in `validate` mode only — it never creates or alters tables.
5. **DTO Separation** — Entities never leak to the controller or template. Java records serve as immutable DTOs with factory methods.
6. **Single Responsibility** — `RuleEngineService` only categorizes. `FinanceTrackerService` orchestrates. `CsvImportService` only parses. `StorageService` only handles file I/O.

---

## 4. Technology Stack & Rationale

| Technology | Version | Why This Choice |
|---|---|---|
| **Spring Boot** | 3.4.2 | Industry-standard framework; auto-configuration reduces boilerplate; mature ecosystem |
| **Java** | 17 (source) / 21 (runtime) | Records for DTOs, sealed types, pattern matching; LTS version |
| **H2 Database** | (managed by Spring Boot) | Zero-install embedded SQL database; file mode persists data across restarts |
| **Flyway** | (managed by Spring Boot) | Version-controlled schema migrations; prevents schema drift between environments |
| **Thymeleaf** | (managed by Spring Boot) | Server-side rendering; no JavaScript framework needed; natural HTML templates |
| **JPA / Hibernate** | (managed by Spring Boot) | ORM for entity persistence; repository abstraction for queries |
| **Bean Validation** | (managed by Spring Boot) | Declarative input validation at the controller boundary |
| **AES-256-GCM** | JDK built-in | Authenticated encryption; no external crypto library needed |
| **PBKDF2WithHmacSHA256** | JDK built-in | Key derivation from passphrase; 600,000 iterations per OWASP recommendation |
| **Maven** | 3.9.12 | Build tool with dependency management; single `mvn spring-boot:run` to start |

### What's NOT in the Stack (and Why)

- **No React/Angular/Vue** — Thymeleaf keeps the frontend simple and avoids a separate build pipeline.
- **No external APIs** — Offline-first means no REST calls, no cloud databases, no third-party services.
- **No Lombok** — Java records replace most Lombok use cases; keeps the code explicit.
- **No Spring Security** — Single-user localhost app; the server only binds to 127.0.0.1 so external access is physically impossible.

---

## 5. Project Structure

```
kanso-note-main/
├── pom.xml                          # Maven build configuration
├── Dockerfile                       # Container build (Cloud Run deployment)
├── cloudbuild.yaml                  # Google Cloud Build pipeline
├── deploy-to-cloud-run.sh           # Cloud Run deploy script
├── CLOUD_RUN_DEPLOYMENT.md          # Deployment documentation
├── README.md                        # Project README
│
├── src/
│   ├── main/
│   │   ├── java/com/bankingoop/finance/
│   │   │   ├── PersonalFinanceTrackerApplication.java    # @SpringBootApplication entry point
│   │   │   │
│   │   │   ├── config/
│   │   │   │   └── StorageConfig.java                    # @PostConstruct directory creation
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   └── DashboardController.java              # Single controller (~280 lines)
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── AnalyticsSummaryDto.java              # Income/expense summary
│   │   │   │   ├── CategoryRuleDto.java                  # Rule presentation
│   │   │   │   ├── CategorySpendDto.java                 # Category spending display
│   │   │   │   ├── MonthlyTrendDto.java                  # Trends with MoM delta + rolling avg
│   │   │   │   ├── RecurringTransactionDto.java          # Recurring transaction info
│   │   │   │   ├── RuleSuggestionDto.java                # Auto-suggested rules
│   │   │   │   ├── TransactionDto.java                   # Transaction display
│   │   │   │   └── UnusualTransactionDto.java            # Anomaly detection display
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── CategoryRuleEntity.java               # JPA entity for rules
│   │   │   │   ├── ProfileEntity.java                    # Single-user profile
│   │   │   │   ├── TransactionEntity.java                # JPA entity for transactions
│   │   │   │   └── UploadedFileEntity.java               # CSV upload audit trail
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── CategoryRule.java                     # Legacy model (pre-JPA)
│   │   │   │   ├── CsvTransactionRow.java                # Parsed CSV row POJO
│   │   │   │   ├── MonthlyTrend.java                     # Legacy model (pre-JPA)
│   │   │   │   ├── Transaction.java                      # Legacy model (pre-JPA)
│   │   │   │   └── UnusualTransaction.java               # Legacy model (pre-JPA)
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── CategoryRuleRepository.java           # Rule queries
│   │   │   │   ├── ProfileRepository.java                # Profile access
│   │   │   │   ├── TransactionRepository.java            # Transaction queries
│   │   │   │   └── UploadedFileRepository.java           # Upload audit queries
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── CsvImportService.java                 # CSV parsing (~100 lines)
│   │   │   │   ├── FinanceTrackerService.java            # Core orchestrator (~530 lines)
│   │   │   │   ├── RuleEngineService.java                # Rule engine (~300 lines)
│   │   │   │   ├── StorageService.java                   # Storage interface
│   │   │   │   └── StorageServiceImpl.java               # Local filesystem implementation
│   │   │   │
│   │   │   └── util/
│   │   │       └── EncryptionUtil.java                   # AES-256-GCM + PBKDF2 (~100 lines)
│   │   │
│   │   └── resources/
│   │       ├── application.properties                    # Main configuration
│   │       ├── sample-transactions.csv                   # 28+ sample transactions
│   │       ├── db/migration/
│   │       │   └── V1__init.sql                          # Flyway migration (4 tables + 20 rules)
│   │       ├── static/
│   │       │   └── styles.css                            # Complete CSS (~450 lines)
│   │       └── templates/
│   │           └── dashboard.html                        # Single-page Thymeleaf template
│   │
│   └── test/
│       ├── java/com/bankingoop/finance/service/
│       │   └── RuleEngineServiceTest.java                # 6 integration tests
│       └── resources/
│           └── application-test.properties               # H2 in-memory test config
│
└── data/                             # Runtime data directory (git-ignored)
    ├── kanso-db.mv.db                # H2 database file
    └── uploads/                      # Stored CSV uploads
```

---

## 6. Database Schema & Migrations

### Flyway Migration: `V1__init.sql`

Flyway manages all schema changes. Hibernate runs in `validate` mode, meaning it **only verifies** the schema matches the entities — it never creates or alters tables. This separation ensures:

- Schema changes are version-controlled and repeatable
- No accidental schema drift between development and production
- All seed data is applied consistently

### Tables

#### `profile` — Single-user configuration

```sql
CREATE TABLE profile (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_name    VARCHAR(100) NOT NULL DEFAULT 'Default User',
    encryption_salt VARCHAR(64),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Stores the single-user profile with display name and encryption salt. A default profile row is seeded on first migration. This table enables future multi-profile support without schema changes.

#### `category_rule` — Categorization rules

```sql
CREATE TABLE category_rule (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    pattern_type VARCHAR(10)  NOT NULL DEFAULT 'KEYWORD',
    pattern      VARCHAR(255) NOT NULL,
    category     VARCHAR(100) NOT NULL,
    priority     INT          NOT NULL DEFAULT 0,
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Each rule defines a pattern (KEYWORD or REGEX), maps to a category, and has a priority for conflict resolution. The `is_default` flag distinguishes seed rules from user-created rules.

#### `transaction` — Financial transactions

```sql
CREATE TABLE transaction (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    date            DATE           NOT NULL,
    description     VARCHAR(500)   NOT NULL,
    amount          DECIMAL(15, 2) NOT NULL,
    category        VARCHAR(100)   NOT NULL DEFAULT 'Uncategorized',
    matched_rule_id BIGINT,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_rule FOREIGN KEY (matched_rule_id)
        REFERENCES category_rule(id) ON DELETE SET NULL
);
CREATE INDEX idx_transaction_date ON transaction(date);
CREATE INDEX idx_transaction_category ON transaction(category);
```

Negative amounts = expenses, positive = income. The `matched_rule_id` FK tracks which rule categorized each transaction, enabling auditability and explainability.

#### `uploaded_file` — CSV upload audit trail

```sql
CREATE TABLE uploaded_file (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_path   VARCHAR(500) NOT NULL,
    encrypted     BOOLEAN      NOT NULL DEFAULT FALSE,
    row_count     INT          NOT NULL DEFAULT 0,
    uploaded_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Every CSV upload is logged with its original filename, storage location, encryption status, and row count. This provides a full audit trail of data imports.

### Seed Data — 20 Default Categorization Rules

| Pattern | Category | Priority | Notes |
|---|---|---|---|
| `walmart`, `target`, `costco`, `kroger` | Groceries | 10 | Major grocery chains |
| `uber`, `lyft` | Transport | 10 | Ride-share |
| `shell`, `chevron` | Fuel | 10 | Gas stations |
| `netflix`, `spotify` | Subscriptions | 10 | Streaming services |
| `rent`, `mortgage` | Housing | 15 | Higher priority — housing is a major category |
| `electric`, `water bill`, `internet` | Utilities | 8 | Household utilities (slightly lower priority) |
| `salary`, `payroll` | Income | 20 | Highest priority — income detection |
| `amazon` | Shopping | 5 | Lower priority — Amazon sells everything |
| `restaurant`, `cafe` | Dining | 10 | Eating out |

The priority values are intentional: `salary` (20) > `rent` (15) > most patterns (10) > `utilities` (8) > `amazon` (5). This means "Salary deposit from Amazon" would be categorized as Income, not Shopping.

---

## 7. Configuration & Profiles

### Main Configuration: `application.properties`

```properties
# Server: localhost-only binding (blocks ALL external access)
server.address=127.0.0.1
server.port=${PORT:8080}

# H2 file-mode: data survives restarts, no install needed
spring.datasource.url=jdbc:h2:file:./data/kanso-db;DB_CLOSE_ON_EXIT=FALSE

# JPA validate mode: Flyway owns the schema
spring.jpa.hibernate.ddl-auto=validate

# Flyway: version-controlled migrations
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# File upload limit: 5MB
spring.servlet.multipart.max-file-size=5MB

# H2 Console: accessible at /h2-console (localhost only)
spring.h2.console.enabled=true
spring.h2.console.settings.web-allow-others=false

# Custom storage paths
kanso.storage.upload-dir=./data/uploads
kanso.storage.db-path=./data/kanso-db
```

**Key security decisions:**
- `server.address=127.0.0.1` — The app literally cannot be accessed from another machine.
- `spring.h2.console.settings.web-allow-others=false` — The H2 console is locked to localhost.
- `DB_CLOSE_ON_EXIT=FALSE` — Database stays open for graceful shutdown.

### Test Configuration: `application-test.properties`

```properties
# H2 in-memory mode: fast, isolated, no file I/O
spring.datasource.url=jdbc:h2:mem:kanso-test;DB_CLOSE_DELAY=-1

# Storage directories under target/ (cleaned by mvn clean)
kanso.storage.upload-dir=./target/test-uploads
kanso.storage.db-path=./target/test-db
```

Tests use an in-memory H2 database that is created fresh for each test class and destroyed afterward. File storage paths point to `target/` so test artifacts are cleaned by `mvn clean`.

---

## 8. Domain Model — Entities

### `TransactionEntity`

The central entity representing a financial transaction.

```java
@Entity
@Table(name = "transaction", indexes = {
    @Index(name = "idx_transaction_date", columnList = "date"),
    @Index(name = "idx_transaction_category", columnList = "category")
})
public class TransactionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private LocalDate date;
    @Column(nullable = false, length = 500) private String description;
    @Column(nullable = false, precision = 15, scale = 2) private BigDecimal amount;
    @Column(length = 100) private String category;
    @Column(name = "matched_rule_id") private Long matchedRuleId;
    @Column(name = "created_at") private LocalDateTime createdAt;
}
```

**Design details:**
- **Amount convention:** Negative = expense, positive = income. Helper methods `isExpense()` and `isIncome()` encapsulate this logic.
- **matched_rule_id:** FK to `category_rule`. Tracks which rule made the categorization decision, enabling full audit trail.
- **Precision:** `DECIMAL(15,2)` — supports amounts up to 9,999,999,999,999.99.
- **Indexes:** On `date` (for date range queries) and `category` (for spending aggregation).
- **@PrePersist:** Automatically sets `createdAt` to `LocalDateTime.now()` on first save.

### `CategoryRuleEntity`

Defines a pattern-to-category mapping used by the rule engine.

```java
@Entity
@Table(name = "category_rule")
public class CategoryRuleEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pattern_type", length = 10) private String patternType; // "KEYWORD" or "REGEX"
    @Column(nullable = false, length = 255) private String pattern;
    @Column(nullable = false, length = 100) private String category;
    @Column private int priority;                    // Higher number wins
    @Column private boolean enabled;
    @Column(name = "is_default") private boolean isDefault;
    @Column(name = "created_at") private LocalDateTime createdAt;
}
```

**Design details:**
- **patternType:** Either `KEYWORD` (case-insensitive substring match) or `REGEX` (Java regex).
- **priority:** Integer priority for conflict resolution. Higher wins. Seed rules range from 5 (`amazon`) to 20 (`salary`).
- **isDefault:** Distinguishes seed rules from user-created rules. Users can only delete non-default rules.
- **enabled:** Toggle rules on/off without deleting them.

### `ProfileEntity`

Single-user profile with encryption metadata.

```java
@Entity
@Table(name = "profile")
public class ProfileEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "encryption_salt", length = 64) private String encryptionSalt;
    @Column(name = "created_at") private LocalDateTime createdAt;
}
```

### `UploadedFileEntity`

Audit trail for CSV uploads.

```java
@Entity
@Table(name = "uploaded_file")
public class UploadedFileEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "original_name", nullable = false) private String originalName;
    @Column(name = "stored_path", nullable = false, length = 500) private String storedPath;
    @Column private boolean encrypted;
    @Column(name = "row_count") private int rowCount;
    @Column(name = "uploaded_at") private LocalDateTime uploadedAt;
}
```

---

## 9. Repository Layer

All repositories extend `JpaRepository` for standard CRUD operations plus custom query methods.

### `TransactionRepository`

```java
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findAllByOrderByDateDescIdDesc();
    List<TransactionEntity> findByDateBetweenOrderByDateDescIdDesc(LocalDate from, LocalDate to);

    @Query("SELECT t FROM TransactionEntity t WHERE t.amount < 0")
    List<TransactionEntity> findAllExpenses();

    @Query("SELECT t FROM TransactionEntity t WHERE t.category = 'Uncategorized' OR t.category IS NULL")
    List<TransactionEntity> findUncategorized();

    @Query("SELECT DISTINCT t.description FROM TransactionEntity t WHERE t.category = 'Uncategorized' OR t.category IS NULL")
    List<String> findDistinctUncategorizedDescriptions();

    @Query("SELECT t.category, COUNT(t) FROM TransactionEntity t GROUP BY t.category")
    List<Object[]> countByCategory();

    List<TransactionEntity> findTop10ByOrderByCreatedAtDesc();
}
```

**Key queries:**
- `findAllExpenses()` — Used by anomaly detection (only analyzes expense transactions).
- `findDistinctUncategorizedDescriptions()` — Used by auto-suggest to find patterns in uncategorized transactions.
- `findByDateBetweenOrderByDateDescIdDesc()` — Used by date range filter and CSV export.

### `CategoryRuleRepository`

```java
public interface CategoryRuleRepository extends JpaRepository<CategoryRuleEntity, Long> {
    List<CategoryRuleEntity> findByEnabledTrueOrderByPriorityDesc();
    List<CategoryRuleEntity> findByIsDefaultFalseOrderByCreatedAtDesc();
    List<CategoryRuleEntity> findByIsDefaultTrueOrderByPriorityDesc();
}
```

### `ProfileRepository` & `UploadedFileRepository`

```java
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {}

public interface UploadedFileRepository extends JpaRepository<UploadedFileEntity, Long> {
    List<UploadedFileEntity> findAllByOrderByUploadedAtDesc();
}
```

---

## 10. DTO Layer — Data Transfer Objects

All DTOs are Java **records** — immutable, concise, with automatic `equals()`, `hashCode()`, and `toString()`. Each includes a static factory method `from(Entity)` where applicable.

### `TransactionDto`

```java
public record TransactionDto(
    Long id, LocalDate date, String description,
    BigDecimal amount, String category, Long matchedRuleId,
    boolean income, boolean expense
) {
    public static TransactionDto from(TransactionEntity e) { ... }
}
```

The `income` and `expense` flags are derived from `amount > 0` and `amount < 0` respectively, used in the template for CSS coloring (green for income, red for expenses).

### `MonthlyTrendDto`

```java
public record MonthlyTrendDto(
    YearMonth month, String label,
    BigDecimal income, BigDecimal expense, BigDecimal net,
    boolean positiveNet,
    BigDecimal momDeltaPercent,      // Month-over-month expense change (%)
    BigDecimal rollingAvgExpense     // 3-month rolling average of expenses
) {
    public static MonthlyTrendDto of(YearMonth month, BigDecimal income,
                                      BigDecimal expense, BigDecimal momDelta,
                                      BigDecimal rollingAvg) { ... }
}
```

### `CategoryRuleDto`

```java
public record CategoryRuleDto(
    Long id, String patternType, String pattern,
    String category, int priority, boolean enabled, boolean isDefault
) {
    public static CategoryRuleDto from(CategoryRuleEntity e) { ... }
}
```

### Other DTOs

| DTO | Fields | Purpose |
|---|---|---|
| `CategorySpendDto` | category, amount, transactionCount | Top spending category display |
| `RecurringTransactionDto` | description, typicalAmount, occurrenceCount, frequency | Recurring transaction detection results |
| `RuleSuggestionDto` | pattern, matchCount, suggestedCategory | Auto-suggested rules from uncategorized transactions |
| `UnusualTransactionDto` | transaction, absoluteAmount, threshold | Anomaly detection results |
| `AnalyticsSummaryDto` | Various analytics fields | Summary analytics container |

---

## 11. Service Layer

### 11.1 Rule Engine Service

**File:** `RuleEngineService.java` (~300 lines)  
**Responsibility:** Deterministic transaction categorization using pattern-matching rules.

#### Categorization Algorithm

```
Input: description, amount, explicitCategory
Output: MatchResult(category, matchedRuleId)

1. If explicitCategory is non-blank → return it (user override)
2. Load all enabled rules, ordered by priority descending
3. For each rule, compute matchLength:
   - KEYWORD: case-insensitive substring search → returns keyword length
   - REGEX: Pattern.compile().matcher().find() → returns matched region length
4. Collect all rules with matchLength > 0
5. Sort matches by:
   - Primary: highest priority first (-priority)
   - Secondary: longest match first (-matchLength)
6. Pick the first match (winner)
7. Fallback: amount ≥ 0 → "Income", else → "Uncategorized"
```

#### Conflict Resolution — The Comparator Fix

The original implementation used chained `.reversed()` calls:
```java
// BUG: .reversed().reversed() silently inverts the sort
matches.sort(Comparator.comparingInt(m -> m.rule().getPriority()).reversed()
             .thenComparingInt(m -> m.matchLength()).reversed());
```

This was broken because `.reversed()` on a `thenComparing` chain reverses the *entire* comparator, not just the last key. The fix uses negation:

```java
// FIXED: negation gives deterministic descending sort
matches.sort(Comparator
    .comparingInt((RuleMatch m) -> -m.rule().getPriority())
    .thenComparingInt((RuleMatch m) -> -m.matchLength()));
```

#### Auto-Suggest Algorithm (D18)

```
1. Query all distinct descriptions from uncategorized transactions
2. For each description, extract the first "meaningful" word (≥3 chars, alpha only)
3. Count occurrences of each keyword across descriptions
4. Return top 10 keywords sorted by frequency (descending)
5. These become suggested rule patterns the user can adopt
```

#### Rule CRUD

- **addCustomRule(patternType, pattern, category, priority)** — Validates regex patterns at creation time. Normalizes category to Title Case.
- **deleteRule(ruleId)** — Hard delete by ID.
- **toggleRule(ruleId, enabled)** — Enable/disable without deletion.
- **normalizeCategory(category)** — Converts "dining out" → "Dining Out" (Title Case).

### 11.2 Finance Tracker Service

**File:** `FinanceTrackerService.java` (~530 lines)  
**Responsibility:** Core orchestrator tying together CSV import, rule engine, persistence, and analytics.

#### Transaction CRUD

- **addManualTransaction(date, description, amount, category)** — Delegates to rule engine for categorization, persists via JPA, records IDs in undo buffer.
- **updateTransaction(id, date, description, amount, category)** — Re-categorizes when description or category changes.
- **deleteTransaction(id)** — Hard delete by ID.
- **clearTransactions()** — Deletes all transactions and resets undo buffer.

#### CSV Import Pipeline

```
1. User uploads CSV file via multipart form
2. StorageService.saveCsv() writes file to ./data/uploads/ (optionally encrypted)
3. CsvImportService.parse() extracts rows with date/description/amount/category
4. Each row → addManualTransaction() → rule engine categorization → JPA persist
5. UploadedFileEntity audit record created with filename, path, encryption status, row count
6. Undo buffer stores all imported transaction IDs
```

#### Monthly Trends Analytics (B4, B6)

```
1. Load all transactions from database
2. Group into income/expense maps keyed by YearMonth
3. For each month, compute:
   a. Total income and total expense
   b. MoM Delta: ((current - previous) / previous) × 100
   c. 3-Month Rolling Average: avg(expenses for months [i-2..i])
4. Return sorted newest-first
```

#### Top Categories (B5)

Groups all expense transactions by category, sums amounts, counts transactions, sorts by amount descending, returns top N.

#### Savings Rate (B7)

```
Savings Rate = ((Total Income - Total Expense) / Total Income) × 100

Color levels:
  ≥ 20% → "good" (green)
  10-20% → "warning" (amber)
  < 10% → "danger" (red)
  No income → "neutral"
```

#### Anomaly Detection

```
1. Collect all expense transactions (amount < 0)
2. Require minimum 5 expenses (otherwise return empty)
3. Compute mean and standard deviation of absolute amounts
4. Set threshold = mean + 2 × stddev (or mean × 1.5 if stddev is 0)
5. Flag transactions where |amount| ≥ threshold AND |amount| ≥ $20
6. Return top 10 sorted by amount descending
```

The $20 floor prevents noise from flagging small transactions. The mean + 2σ threshold catches approximately the top 2.5% of expenses in a normal distribution.

#### Recurring Transaction Detection (C9)

```
1. Load all transactions
2. Group by normalized description (lowercase, trimmed)
3. For each group with ≥ 2 transactions:
   a. Count distinct months of occurrence
   b. If < 2 distinct months → skip (not recurring)
   c. Compute average amount across the group
   d. Check 5% tolerance: every transaction's |amount| is within 5% of the average
   e. If consistent → mark as recurring
4. Frequency label: "Monthly" if distinct months ≥ transaction count, else "Recurring"
5. Sort by occurrence count descending
```

#### Undo (E23)

Single-step undo using an in-memory buffer:
- `lastAddedIds` — List of transaction IDs from the last add/import action.
- `lastActionDescription` — Human-readable description of the action.
- `undoLastAction()` — Deletes all IDs in the buffer and clears it.
- `canUndo()` — Returns true if the buffer is non-empty.

**Trade-off:** This is simpler than a full Command pattern but only supports undoing the most recent action. Sufficient for a single-user offline app.

#### CSV Export (C11)

```java
public String exportToCsv(LocalDate from, LocalDate to) {
    // Queries transactions with optional date range
    // Builds CSV string: date,description,amount,category
    // Properly escapes double quotes in descriptions
}
```

### 11.3 CSV Import Service

**File:** `CsvImportService.java` (~100 lines)  
**Responsibility:** Parsing CSV files with flexible date and amount formats.

#### Supported Date Formats

```java
List<DateTimeFormatter> DATE_FORMATS = List.of(
    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    DateTimeFormatter.ofPattern("M/d/yyyy"),
    DateTimeFormatter.ofPattern("yyyy/MM/dd")
);
```

#### Amount Parsing

- Handles `$` prefix stripping
- Handles parenthesized negatives: `(50.00)` → `-50.00`
- Handles comma-separated thousands: `1,234.56` → `1234.56`

#### Header Detection

Automatically detects column positions by header names (`date`, `description`, `amount`, `category`). Falls back to positional (columns 0-3) if no header row is found.

### 11.4 Storage Service

**File:** `StorageService.java` (interface) + `StorageServiceImpl.java` (~180 lines)  
**Responsibility:** Local file system operations with optional encryption.

#### Interface Methods

```java
public interface StorageService {
    Path saveCsv(MultipartFile file, char[] passphrase) throws IOException, GeneralSecurityException;
    List<Path> listUploads();
    byte[] readUpload(String fileName, char[] passphrase) throws IOException, GeneralSecurityException;
    void encryptDatabase(char[] passphrase) throws IOException, GeneralSecurityException;
    void decryptDatabase(char[] passphrase) throws IOException, GeneralSecurityException;
}
```

#### Security Features

- **Path traversal prevention:** Every resolved path is checked with `target.startsWith(uploadDir)` to prevent directory escape attacks.
- **Filename sanitization:** Original filenames are cleaned of dangerous characters before storage.
- **Timestamp prefixing:** Stored files are named `{timestamp}_{original-name}[.enc]` to prevent collisions.
- **Passphrase handling:** Passphrases are accepted as `char[]` (not String) to allow zeroing after use. The passphrase is never stored on disk.

---

## 12. Controller Layer

**File:** `DashboardController.java` (~280 lines)  
**Single controller** handling all HTTP endpoints through Thymeleaf server-side rendering.

### Request Flow

```
Browser → GET / → DashboardController.dashboard()
                    ├── Parse optional ?from=&to= date range
                    ├── financeService.getTransactions(from, to)
                    ├── financeService.getMonthlyTrends()
                    ├── financeService.getTopCategories(3)
                    ├── financeService.detectUnusualTransactions()
                    ├── financeService.getCurrentMonthCategorySpend()
                    ├── financeService.detectRecurringTransactions()
                    ├── financeService.getSavingsRatePercent()
                    ├── ruleEngineService.getCustomRules()
                    ├── ruleEngineService.getDefaultRules()
                    ├── ruleEngineService.suggestRules()
                    ├── financeService.canUndo()
                    └── model.addAttribute(...) → dashboard.html
```

### Model Attributes Populated

| Attribute | Type | Description |
|---|---|---|
| `transactions` | `List<TransactionDto>` | Filtered by date range |
| `transactionCount` | `int` | Total count |
| `totalIncome` | `BigDecimal` | Sum of positive amounts |
| `totalExpense` | `BigDecimal` | Sum of absolute negative amounts |
| `netFlow` | `BigDecimal` | Income - Expense |
| `netPositive` | `boolean` | Is net > 0? |
| `monthlyTrends` | `List<MonthlyTrendDto>` | With MoM delta + rolling avg |
| `unusualTransactions` | `List<UnusualTransactionDto>` | Anomaly detection results |
| `topCategories` | `List<CategorySpendDto>` | Top 3 spending categories |
| `categorySpend` | `Map<String, BigDecimal>` | Current month by category |
| `recurringTransactions` | `List<RecurringTransactionDto>` | Recurring patterns |
| `savingsRate` | `BigDecimal` | Savings rate percentage |
| `savingsRateLevel` | `String` | "good" / "warning" / "danger" |
| `customRules` | `List<CategoryRuleDto>` | User-created rules |
| `defaultRules` | `List<CategoryRuleDto>` | Seed rules |
| `ruleSuggestions` | `List<RuleSuggestionDto>` | Auto-suggested rules |
| `canUndo` | `boolean` | Is undo available? |
| `lastAction` | `String` | Description of last action |
| `isFirstUse` | `boolean` | Show onboarding wizard? |
| `today` | `LocalDate` | Default date for form |
| `filterFrom` / `filterTo` | `String` | Current filter values |

---

## 13. Security & Encryption

### `EncryptionUtil.java` — AES-256-GCM + PBKDF2

#### Key Derivation

```
Algorithm: PBKDF2WithHmacSHA256
Iterations: 600,000 (per OWASP 2023 recommendation)
Salt: 16 bytes, cryptographically random
Key length: 256 bits
```

#### Encryption

```
Algorithm: AES/GCM/NoPadding
IV: 12 bytes, cryptographically random
Tag length: 128 bits (built into GCM mode)

Output format: [16-byte salt][12-byte IV][ciphertext + authentication tag]
```

#### Threat Model (Documented in Source)

```
What this protects: Data at rest on the local filesystem.
What this does NOT protect: Data in memory while the app is running;
screen captures; OS-level access by a logged-in user.

Acceptable for: Single-user offline app where the threat is
"someone copies my files." NOT suitable for multi-tenant
server-side encryption without HSM/key management.
```

### Other Security Measures

| Measure | Implementation |
|---|---|
| **Localhost-only binding** | `server.address=127.0.0.1` in application.properties |
| **Path traversal prevention** | `target.startsWith(uploadDir)` check in StorageServiceImpl |
| **Filename sanitization** | Strips special characters from uploaded filenames |
| **H2 console restricted** | `spring.h2.console.settings.web-allow-others=false` |
| **File upload size limit** | `spring.servlet.multipart.max-file-size=5MB` |
| **Regex validation** | `Pattern.compile()` validation before saving REGEX rules |
| **Input validation** | Null/blank checks on all service method inputs |
| **Confirmation dialogs** | `onsubmit="return confirm(...)"` on destructive operations |

---

## 14. Frontend — Thymeleaf Templates & CSS

### `dashboard.html` — Single-Page Dashboard

The entire UI is a single Thymeleaf template rendered server-side on each request. No JavaScript framework, no SPA, no API calls. Sections appear/disappear based on data state.

#### Layout Structure

```
┌──────────── Hero (Kanso branding, author credit) ─────────────┐
├──────────── Alerts (success/error messages) ──────────────────┤
├──────────── Onboarding Wizard (E20, first-use only) ──────────┤
├──────────── Undo Banner (E23, when undo available) ───────────┤
├──────────── Summary Stats Grid (5 cards) ─────────────────────┤
│  Transactions │ Total Income │ Total Expenses │ Net │ Savings  │
├──────────── Top Spending Categories (B5) ─────────────────────┤
├──────────── Input Section (3 cards) ──────────────────────────┤
│  Add Transaction │ Import CSV │ Add Rule + Suggestions        │
├──────────── Insights Section (4 cards) ───────────────────────┤
│  Monthly Trends │ Unusual Txns │ Category Spend │ Recurring   │
├──────────── Transactions Table (with filter + export) ────────┤
│  Date Filter │ Export CSV │ Full table with Edit/Delete       │
├──────────── Help Section (non-first-use) ─────────────────────┤
└──────────── Footer (credits, version, copyright) ─────────────┘
```

#### Feature-Specific UI Elements

- **Onboarding (E20):** Green-bordered card with 3-step instructions and "Load Sample Data" button. Only shows when `transactionCount == 0`.
- **Undo (E23):** Amber banner with last action description and "Undo" button. Only shows when `canUndo == true`.
- **Savings Rate (B7):** Color-coded stat card — green (≥20%), amber (10-20%), red (<10%).
- **Top Categories (B5):** Horizontal bar display with category name, dollar amount, and transaction count.
- **Monthly Trends Table (B4, B6):** Columns for Month, Income, Expenses, Net, MoM Δ%, 3m Avg.
- **Inline Edit (E22):** Hidden `<tr>` rows toggled by JavaScript `toggleEdit(id)` function. Each edit row contains a form with date, description, amount, category fields.
- **Date Filter (C12):** From/To date inputs with Filter and Reset controls.
- **Export (C11):** "Export CSV" link passing current filter parameters.
- **Rule Management (D15, D16, D18):** Pattern type dropdown (KEYWORD/REGEX), priority input, suggested rules list.

### `styles.css` — Design System (~450 lines)

#### CSS Variables

```css
:root {
    --bg: #f6f7fb;        /* Page background */
    --surface: #ffffff;    /* Card background */
    --ink: #1e2633;        /* Primary text */
    --muted: #5b6473;      /* Secondary text */
    --line: #dde3ec;       /* Borders */
    --ok: #18794e;         /* Income/success (green) */
    --warn: #bf2600;       /* Expense/danger (red) */
    --warning-mid: #b5850b; /* Warning (amber) */
    --primary: #0f4c81;    /* Brand blue */
}
```

#### Responsive Grid

```css
.summary-grid { grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); }
.inputs       { grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); }
.insights     { grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); }
```

All grids use `auto-fit` with `minmax()` for responsive behavior without media queries (except the footer which has a `@media (max-width: 760px)` breakpoint).

#### Visual Design

- **Cards:** White background, rounded borders (`border-radius: 12px`), subtle box shadows.
- **Hero section:** Gradient background with left blue accent border.
- **Buttons:** Rounded (`border-radius: 9px`) with hover lift effect (`translateY(-1px)`).
- **Typography:** Segoe UI / Tahoma font stack.
- **Color coding:** `.income` (green, bold), `.expense` (red, bold), `.warning-text` (amber, bold).

---

## 15. Feature Catalogue

### Group A — JPA Persistence

| ID | Feature | Status |
|---|---|---|
| A1 | JPA entity persistence (4 entities, 4 tables) | ✅ Implemented |
| A2 | CSV import with StorageService + audit trail | ✅ Implemented |
| A3 | Local H2 file-mode database with Flyway migrations | ✅ Implemented |

### Group B — Analytics

| ID | Feature | Status |
|---|---|---|
| B4 | Month-over-month expense delta (%) | ✅ Implemented |
| B5 | Top-3 spending categories display | ✅ Implemented |
| B6 | 3-month rolling average of expenses | ✅ Implemented |
| B7 | Savings rate with color-coded thresholds | ✅ Implemented |

### Group C — Recurring / Export / Filter

| ID | Feature | Status |
|---|---|---|
| C9 | Recurring transaction detection (5% tolerance) | ✅ Implemented |
| C11 | CSV export with date range support | ✅ Implemented |
| C12 | Date range filter on transaction list | ✅ Implemented |

### Group D — Rule Engine

| ID | Feature | Status |
|---|---|---|
| D15 | REGEX pattern type support | ✅ Implemented |
| D16 | Priority-based conflict resolution | ✅ Implemented |
| D18 | Auto-suggest rules from uncategorized transactions | ✅ Implemented |

### Group E — UX

| ID | Feature | Status |
|---|---|---|
| E20 | First-use onboarding wizard | ✅ Implemented |
| E22 | Inline edit/delete on transaction rows | ✅ Implemented |
| E23 | Single-step undo for last action | ✅ Implemented |

**Total: 15 features implemented across 5 groups.**

---

## 16. Algorithms & Technical Deep-Dives

### 16.1 Rule Engine Conflict Resolution

**Problem:** Multiple rules can match the same transaction description. Example: "Uber Eats delivery" matches both `uber` → Transport and `eats` → Dining.

**Solution:** Deterministic two-key sort:
1. **Primary key:** Priority (descending). Higher priority rules win.
2. **Secondary key:** Match length (descending). Longer matches are more specific.

**Implementation detail:** The comparator uses negation (`-priority`, `-matchLength`) instead of `.reversed()` because chained `.reversed()` calls in Java's Comparator API silently invert the entire sort order — a bug caught by integration tests.

### 16.2 Anomaly Detection (Mean + 2σ)

**Statistical basis:** In a normal distribution, ~95.4% of values fall within μ ± 2σ. Transactions exceeding mean + 2σ are in the top ~2.5%, making them statistically unusual.

**Implementation guards:**
- Minimum 5 expenses required (too few data points makes σ meaningless).
- $20 floor prevents flagging trivial transactions like a $3 coffee.
- If σ = 0 (all expenses identical), fallback to threshold = mean × 1.5.

### 16.3 Recurring Transaction Detection

**Approach:** Frequency analysis + amount consistency.

```
Group by: normalized description (lowercase, trimmed)
Filters:
  - ≥ 2 transactions in the group
  - ≥ 2 distinct months of occurrence
  - All amounts within 5% of group average
Classification:
  - "Monthly" if (distinct months ≥ transaction count)
  - "Recurring" otherwise
```

**Why 5% tolerance?** Subscription prices fluctuate slightly (taxes, promo changes). $9.99 and $10.49 should still be grouped together.

### 16.4 Savings Rate Calculation

```
Rate = ((Income - Expense) / Income) × 100

Thresholds (aligned with common financial advice):
  ≥ 20% → "good"    (50/30/20 rule benchmark)
  10-20% → "warning" (below optimal)
  < 10% → "danger"   (at risk of insufficient savings)
```

### 16.5 Month-over-Month Delta

```
MoM Δ = ((Current Month Expense - Previous Month Expense) / Previous Month Expense) × 100

Positive delta → spending increased (shown in red)
Negative delta → spending decreased (shown in green)
Null → no previous month data available (shown as "—")
```

### 16.6 3-Month Rolling Average

For month index `i`, the rolling average is:

$$\text{Rolling Avg}_i = \frac{\sum_{j=\max(0, i-2)}^{i} \text{Expense}_j}{\min(3, i+1)}$$

This smooths out monthly volatility and helps identify spending trends.

---

## 17. Testing Strategy

### Test Configuration

- **Framework:** JUnit 5 + Spring Boot Test
- **Profile:** `@ActiveProfiles("test")` → H2 in-memory database
- **Isolation:** `@Transactional` on each test → auto-rollback after each test
- **Database:** `jdbc:h2:mem:kanso-test;DB_CLOSE_DELAY=-1`

### `RuleEngineServiceTest` — 6 Integration Tests

| Test | What It Verifies |
|---|---|
| `categorize_keywordMatch_returnsCorrectCategory` | Basic keyword matching: "Walmart groceries" → "Groceries" |
| `categorize_explicitCategory_overridesRuleEngine` | Explicit category bypasses all rules |
| `categorize_noMatch_returnsUncategorized` | Unknown description → "Uncategorized" fallback |
| `categorize_conflictResolution_highestPriorityWins` | Priority 50 beats priority 10 on same description |
| `categorize_conflictResolution_longestMatchWins` | Same priority: "uber eats" (9 chars) beats "uber" (4 chars) |
| `categorize_regexMatch_returnsCorrectCategory` | REGEX pattern `(?i)starbucks\|sbux` matches "SBUX order" |

### Test Implementation Details

- Tests add custom rules via `ruleEngineService.addCustomRule()` and call `categoryRuleRepository.flush()` to ensure the rules are visible within the same `@Transactional` test.
- The flush was necessary because JPA's first-level cache wouldn't otherwise expose newly added rules to subsequent repository queries within the same transaction.

### Running Tests

```bash
mvn test
# Or specifically:
mvn test -Dtest=RuleEngineServiceTest
```

---

## 18. Build, Run & Deploy

### Prerequisites

- **JDK 17+** (project source level is 17; tested with JDK 21.0.10)
- **Maven 3.9+**

### Local Development

```bash
# Set environment (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:PATH = "C:\Users\v-srajoju\.maven\maven-3.9.12\bin;$env:PATH"

# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run

# Access
# Open http://localhost:8080 in a browser
```

### Build Commands

| Command | Purpose |
|---|---|
| `mvn clean compile` | Compile all sources |
| `mvn test` | Run integration tests (H2 in-memory) |
| `mvn clean package` | Build executable JAR |
| `mvn spring-boot:run` | Start the application |
| `java -jar target/kanso-1.0.0.jar` | Run packaged JAR directly |

### Docker

```dockerfile
# Dockerfile is included for Cloud Run deployment
FROM eclipse-temurin:17-jre-jammy
COPY target/kanso-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Cloud Run

The project includes `cloudbuild.yaml`, `deploy-to-cloud-run.sh`, and `CLOUD_RUN_DEPLOYMENT.md` for Google Cloud Run deployment. The `PORT` environment variable is read via `${PORT:8080}` in application.properties.

---

## 19. Sample Data

### `sample-transactions.csv`

29 transactions spanning January–February 2026, designed to exercise every feature:

| Category | Example Transactions |
|---|---|
| **Income** | Monthly Salary ($4,200) × 2 months, Freelance Payment ($850), Bonus ($600) |
| **Housing** | Rent Payment (-$1,600) × 2 months |
| **Groceries** | Costco Wholesale, Whole Foods, Target Grocery, Trader Joe's |
| **Transport** | Uber ride, Shell Gas Station, Lyft to Airport, Chevron |
| **Subscriptions** | Netflix |
| **Dining** | McDonald's, Restaurant Downtown, Chipotle, Valentine Dinner |
| **Utilities** | Water Bill × 2, Electric Bill × 2 |
| **Shopping** | Walmart Supercenter, Amazon, Best Buy Electronics |
| **Fitness** | Gym Membership |
| **Entertainment** | Apple App Store |
| **Anomaly** | Emergency Car Repair (-$920.00) — designed to trigger anomaly detection |

The sample data specifically includes:
- **Recurring patterns:** Salary, rent, and Netflix appear in both months (triggers recurring detection)
- **Anomaly trigger:** The $920 car repair is designed to exceed the mean + 2σ threshold
- **Rule engine coverage:** Descriptions match all 20 seed rules
- **Multiple categories:** Ensures monthly trends and top categories have meaningful data

---

## 20. API / Endpoint Reference

All endpoints are synchronous, server-rendered HTML (except CSV export which returns a file).

### GET Endpoints

| Endpoint | Parameters | Description |
|---|---|---|
| `GET /` | `?from=yyyy-MM-dd&to=yyyy-MM-dd` (optional) | Main dashboard with all analytics |
| `GET /export/csv` | `?from=&to=` (optional) | Download transactions as CSV file |

### POST Endpoints

| Endpoint | Parameters | Description |
|---|---|---|
| `POST /transactions/manual` | `date`, `description`, `amount`, `category` | Add single transaction |
| `POST /transactions/edit` | `id`, `date`, `description`, `amount`, `category` | Update existing transaction |
| `POST /transactions/delete` | `id` | Delete single transaction |
| `POST /transactions/upload` | `file` (multipart) | Import CSV file |
| `POST /transactions/load-sample` | — | Load 28+ sample transactions |
| `POST /transactions/clear` | — | Delete ALL transactions |
| `POST /transactions/undo` | — | Undo last add/import action |
| `POST /rules` | `patternType`, `pattern`, `category`, `priority` | Add categorization rule |

### Response Pattern

All POST endpoints redirect to `GET /` with a success/error message via URL query parameter:
```
redirect:/?message=URL-encoded+success+message
redirect:/?error=URL-encoded+error+message
```

---

## 21. Design Decisions & Trade-offs

### Decision 1: H2 File Mode vs. PostgreSQL

**Chose:** H2 file mode  
**Why:** Zero installation, single-user, offline-first. The user doesn't need to install a database server.  
**Trade-off:** No concurrent access, limited query optimizer.  
**Migration path:** Change the JDBC URL in application.properties and Flyway handles the rest.

### Decision 2: Thymeleaf SSR vs. React SPA

**Chose:** Thymeleaf server-side rendering  
**Why:** No JavaScript build pipeline, no API versioning, no CORS. The full page renders in one request.  
**Trade-off:** No partial updates — every action causes a full page reload.  
**Acceptable because:** A finance tracker doesn't need real-time updates.

### Decision 3: In-Memory Undo vs. Command Pattern

**Chose:** Simple ID buffer (in-memory list of last-added IDs)  
**Why:** Sufficient for "undo the last bulk import" use case. Full Command pattern would add 200+ lines of code for marginal benefit.  
**Trade-off:** Only one undo step. Undo state is lost on server restart.

### Decision 4: Java-Side Analytics vs. SQL Aggregation

**Chose:** Load all transactions into Java, compute analytics there  
**Why:** ~100K transactions is fast enough in Java; keeps the code simple and testable without complex JPQL.  
**Trade-off:** Doesn't scale to millions of transactions.  
**Acceptable because:** Single-user finance tracker — unlikely to exceed 10K transactions.

### Decision 5: Negation Comparator vs. `.reversed()`

**Chose:** `Comparator.comparingInt(m -> -m.priority())` over `.reversed()`  
**Why:** Chained `.reversed()` on `thenComparing` reverses the *entire* comparator, not just the last key. This caused a silent bug where lower-priority rules won.  
**Lesson:** Always verify Comparator behavior under conflict conditions with integration tests.

### Decision 6: PBKDF2 (600K iterations) vs. Argon2

**Chose:** PBKDF2  
**Why:** Available in standard JDK — no external crypto library needed. 600K iterations follows OWASP's 2023 recommendation.  
**Trade-off:** Argon2 is theoretically more GPU-resistant. For an offline app, PBKDF2 is sufficient.

### Decision 7: `char[]` for Passphrases vs. `String`

**Chose:** `char[]`  
**Why:** Strings are immutable and may linger in the JVM string pool. `char[]` can be zeroed after use, reducing the window for memory-dump attacks.  
**Implementation note:** The calling code should zero the array after the encryption/decryption call.

---

## 22. Future Roadmap

Potential enhancements that the architecture supports but are not yet implemented:

| Feature | Difficulty | Architecture Impact |
|---|---|---|
| Budget limits per category | Low | New table + service method, no arch changes |
| Multi-month comparison charts | Low | Frontend only (add Chart.js to template) |
| Receipt image storage | Medium | Extend StorageService, new entity |
| Bank CSV format presets (Chase, Amex, etc.) | Medium | CsvImportService strategy pattern |
| Multi-profile support | Medium | ProfileEntity already exists; add session mgmt |
| REST API layer | Medium | Add @RestController alongside existing controller |
| PostgreSQL migration | Low | Change JDBC URL; Flyway handles schema |
| Dark mode | Low | CSS variables already in place |
| Rule import/export | Low | JSON serialization of CategoryRuleEntity list |
| Transaction search/filter by description | Low | Add repository query + form field |

---

*Documentation generated for Kanso v2.0.0 — Personal Finance Tracker by Sai Uttej R*
