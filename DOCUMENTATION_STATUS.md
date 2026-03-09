# BankingOOP Java Documentation Status Report

## Overview
This report analyzes the documentation status of all Java files in the BankingOOP project across 10 directories. Each file is assessed for class-level documentation and method-level documentation (JavaDoc or 1-liner comments).

---

## 1. CONFIG DIRECTORY
**Path**: `config/`

### ✅ CacheConfig.java
**Class**: `CacheConfig`
- **Class Doc**: ✅ YES - "In-memory caching configuration using Caffeine for analytics computations."
- **Methods**:
  - `cacheManager()` - ❌ NO DOCUMENTATION

### ✅ OpenApiConfig.java
**Class**: `OpenApiConfig`
- **Class Doc**: ✅ YES - "OpenAPI/Swagger configuration — auto-generated API documentation."
- **Methods**:
  - `kansoOpenAPI()` - ❌ NO DOCUMENTATION

### ✅ StorageConfig.java
**Class**: `StorageConfig`
- **Class Doc**: ✅ YES - "Configuration for offline file storage and directory initialization."
- **Methods**:
  - `initDirectories()` - ❌ NO DOCUMENTATION
  - `createDir(Path)` - ❌ NO DOCUMENTATION

### ✅ WebSecurityConfig.java
**Class**: `WebSecurityConfig`
- **Class Doc**: ✅ YES - "Web security configuration with CORS policy and security headers."
- **Methods**:
  - `corsFilter()` - ❌ NO DOCUMENTATION
  - `securityHeadersFilter()` - ❌ NO DOCUMENTATION
  - `doFilter(...)` - ❌ NO DOCUMENTATION

---

## 2. CONTROLLER DIRECTORY
**Path**: `controller/`

### ✅ ApiController.java
**Class**: `ApiController`
- **Class Doc**: ✅ YES - "RESTful API controller — JSON endpoints for programmatic access."
- **Methods** (Note: Most have OpenAPI @Operation annotations but no JavaDoc):
  - `getTransactions(...)` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `createTransaction(...)` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `updateTransaction(...)` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `deleteTransaction(...)` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `getAnalyticsSummary()` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `getMonthlyTrends()` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `getAnomalies()` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `getRecurring()` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `getTopCategories(...)` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `getBudgets()` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `getBudgetStatuses()` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `createBudget(...)` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `updateBudget(...)` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `deleteBudget(...)` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `getRules()` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `getRuleSuggestions()` - ⚠️ HAS ANNOTATIONS, NO JAVADOC
  - `getRecentActivity(...)` - ⚠️ HAS ANNOTATIONS, NO JAVADOC

### ❌ DashboardController.java
**Class**: `DashboardController`
- **Class Doc**: ❌ NO DOCUMENTATION
- **Methods**:
  - `dashboard(...)` - ❌ NO DOCUMENTATION
  - `addManualTransaction(...)` - ❌ NO DOCUMENTATION
  - `editTransaction(...)` - ❌ NO DOCUMENTATION
  - `deleteTransaction(...)` - ❌ NO DOCUMENTATION
  - `uploadTransactions(...)` - ❌ NO DOCUMENTATION
  - `loadSampleData()` - ❌ NO DOCUMENTATION
  - `clearTransactions()` - ❌ NO DOCUMENTATION
  - `clearTransactionsGetFallback()` - ❌ NO DOCUMENTATION
  - `undoLastAction()` - ❌ NO DOCUMENTATION
  - `parseOptionalDate(...)` - ❌ NO DOCUMENTATION
  - `populateModel(...)` - ❌ NO DOCUMENTATION
  - `redirectWithMessage(...)` - ❌ NO DOCUMENTATION
  - `redirectWithError(...)` - ❌ NO DOCUMENTATION

---

## 3. DTO DIRECTORY
**Path**: `dto/`

### ✅ AnalyticsSummaryDto.java
**Class**: `AnalyticsSummaryDto` (record)
- **Class Doc**: ✅ YES - "DTO aggregating key financial metrics for the analytics dashboard."
- **Fields**: No method documentation needed (record auto-generates getters)

### ✅ AuditLogDto.java
**Class**: `AuditLogDto` (record)
- **Class Doc**: ✅ YES - "Audit log DTO — carries audit trail data to the view/API layer."
- **Methods**:
  - `from(AuditLogEntity)` - ❌ NO DOCUMENTATION

### ✅ BudgetDto.java
**Class**: `BudgetDto` (record)
- **Class Doc**: ✅ YES - "Budget DTO — carries budget data to the view/API layer."
- **Methods**:
  - `from(BudgetEntity)` - ❌ NO DOCUMENTATION

### ✅ BudgetStatusDto.java
**Class**: `BudgetStatusDto` (record)
- **Class Doc**: ✅ YES - "Budget status DTO combining budget with real-time spending data and alert status."
- **Methods**:
  - `isExceeded()` - ❌ NO DOCUMENTATION
  - `isWarning()` - ❌ NO DOCUMENTATION

### ✅ CategoryRuleDto.java
**Class**: `CategoryRuleDto` (record)
- **Class Doc**: ✅ YES - "DTO for category rules displayed in UI and REST endpoints. Exposes only what the view needs..."
- **Methods**:
  - `from(CategoryRuleEntity)` - ❌ NO DOCUMENTATION

### ✅ CategorySpendDto.java
**Class**: `CategorySpendDto` (record)
- **Class Doc**: ✅ YES - "DTO for top spending categories with amount and transaction count."
- **Fields**: No documentation needed (record)

### ✅ MonthlyTrendDto.java
**Class**: `MonthlyTrendDto` (record)
- **Class Doc**: ✅ YES - "DTO for monthly income/expense trends with month-over-month delta and rolling average."
- **Methods**:
  - `of(...)` - ❌ NO DOCUMENTATION

### ✅ RecurringTransactionDto.java
**Class**: `RecurringTransactionDto` (record)
- **Class Doc**: ✅ YES - "DTO for recurring transaction patterns based on description and amount similarity..."
- **Fields**: No documentation needed (record)

### ✅ RuleSuggestionDto.java
**Class**: `RuleSuggestionDto` (record)
- **Class Doc**: ✅ YES - "DTO for auto-suggested rules based on uncategorized transaction clusters."
- **Fields**: No documentation needed (record)

### ✅ TransactionDto.java
**Class**: `TransactionDto` (record)
- **Class Doc**: ✅ YES - "DTO for transactions, separating domain entity from view/API representation..."
- **Methods**:
  - `from(TransactionEntity)` - ❌ NO DOCUMENTATION

### ✅ UnusualTransactionDto.java
**Class**: `UnusualTransactionDto` (record)
- **Class Doc**: ✅ YES - "DTO displaying a flagged transaction with anomaly threshold and amount."
- **Fields**: No documentation needed (record)

---

## 4. ENTITY DIRECTORY
**Path**: `entity/`

### ✅ AuditLogEntity.java
**Class**: `AuditLogEntity`
- **Class Doc**: ✅ YES - "Immutable audit log entry recording all significant user actions."
- **Methods** (Getters/Setters):
  - `getId()` - ❌ NO DOCUMENTATION
  - `getEventType()`, `setEventType()` - ❌ NO DOCUMENTATION
  - `getEntityType()`, `setEntityType()` - ❌ NO DOCUMENTATION
  - `getEntityId()`, `setEntityId()` - ❌ NO DOCUMENTATION
  - `getDetails()`, `setDetails()` - ❌ NO DOCUMENTATION
  - `getMetadata()`, `setMetadata()` - ❌ NO DOCUMENTATION
  - `getCreatedAt()` - ❌ NO DOCUMENTATION
  - `onCreate()` - ❌ NO DOCUMENTATION

### ✅ BudgetEntity.java
**Class**: `BudgetEntity`
- **Class Doc**: ✅ YES - "Budget entity with per-category monthly spending limit and alert threshold."
- **Methods** (Getters/Setters/Lifecycle):
  - `getId()`, `setId()` - ❌ NO DOCUMENTATION
  - `getCategory()`, `setCategory()` - ❌ NO DOCUMENTATION
  - `getMonthlyLimit()`, `setMonthlyLimit()` - ❌ NO DOCUMENTATION
  - `getAlertThreshold()`, `setAlertThreshold()` - ❌ NO DOCUMENTATION
  - `isEnabled()`, `setEnabled()` - ❌ NO DOCUMENTATION
  - `getCreatedAt()`, `getUpdatedAt()` - ❌ NO DOCUMENTATION
  - `onCreate()` - ❌ NO DOCUMENTATION
  - `onUpdate()` - ❌ NO DOCUMENTATION

### ✅ CategoryRuleEntity.java
**Class**: `CategoryRuleEntity`
- **Class Doc**: ✅ YES - "Persistent categorization rule with priority and pattern type."
- **Methods**:
  - Constructors - ❌ NO DOCUMENTATION
  - All getters/setters - ❌ NO DOCUMENTATION
  - `onCreate()` - ❌ NO DOCUMENTATION

### ✅ ProfileEntity.java
**Class**: `ProfileEntity`
- **Class Doc**: ✅ YES - "Single-user offline profile with display name and optional encryption salt."
- **Methods**:
  - All getters/setters - ❌ NO DOCUMENTATION
  - `onCreate()`, `onUpdate()` - ❌ NO DOCUMENTATION

### ✅ TransactionEntity.java
**Class**: `TransactionEntity`
- **Class Doc**: ✅ YES - "Transaction entity persisted to local H2 database with category mapping."
- **Methods**:
  - Constructors - ❌ NO DOCUMENTATION
  - `isExpense()` - ❌ NO DOCUMENTATION
  - `isIncome()` - ❌ NO DOCUMENTATION
  - `onCreate()` - ❌ NO DOCUMENTATION
  - All getters/setters - ❌ NO DOCUMENTATION

### ✅ UploadedFileEntity.java
**Class**: `UploadedFileEntity`
- **Class Doc**: ✅ YES - "Audit trail for CSV uploads with file metadata and encryption status."
- **Methods**:
  - All getters/setters - ❌ NO DOCUMENTATION
  - `onCreate()` - ❌ NO DOCUMENTATION

---

## 5. EXCEPTION DIRECTORY
**Path**: `exception/`

### ✅ DuplicateResourceException.java
**Class**: `DuplicateResourceException`
- **Class Doc**: ✅ YES - "Thrown when a budget already exists for a given category."
- **Methods**:
  - Constructor - ❌ NO DOCUMENTATION
  - `getResourceType()` - ❌ NO DOCUMENTATION
  - `getConflictField()` - ❌ NO DOCUMENTATION

### ✅ GlobalExceptionHandler.java
**Class**: `GlobalExceptionHandler`
- **Class Doc**: ✅ YES - "Centralized exception handling for REST API and web controllers."
- **Methods**:
  - `handleNotFound(...)` - ❌ NO DOCUMENTATION
  - `handleDuplicate(...)` - ❌ NO DOCUMENTATION
  - `handleBadRequest(...)` - ❌ NO DOCUMENTATION
  - `handleGeneral(...)` - ❌ NO DOCUMENTATION
  - `buildResponse(...)` - ❌ NO DOCUMENTATION

### ✅ ResourceNotFoundException.java
**Class**: `ResourceNotFoundException`
- **Class Doc**: ✅ YES - "Thrown when a requested resource (transaction, budget, rule) is not found."
- **Methods**:
  - Constructor - ❌ NO DOCUMENTATION
  - `getResourceType()` - ❌ NO DOCUMENTATION
  - `getResourceId()` - ❌ NO DOCUMENTATION

---

## 6. EVENT DIRECTORY
**Path**: `event/`

### ✅ BudgetEvent.java
**Class**: `BudgetEvent`
- **Class Doc**: ✅ YES - "Application event published when a budget is created, updated, or when spending exceeds a threshold."
- **Methods**:
  - Constructor - ❌ NO DOCUMENTATION
  - All getters - ❌ NO DOCUMENTATION

### ✅ TransactionEvent.java
**Class**: `TransactionEvent`
- **Class Doc**: ✅ YES - "Application event published on transaction lifecycle changes..."
- **Methods**:
  - Constructor - ❌ NO DOCUMENTATION
  - All getters - ❌ NO DOCUMENTATION

---

## 7. MODEL DIRECTORY
**Path**: `model/`

### ❌ CategoryRule.java
**Class**: `CategoryRule`
- **Class Doc**: ❌ NO DOCUMENTATION
- **Methods**:
  - Constructor - ❌ NO DOCUMENTATION
  - `getKeyword()` - ❌ NO DOCUMENTATION
  - `getCategory()` - ❌ NO DOCUMENTATION

### ❌ CsvTransactionRow.java
**Class**: `CsvTransactionRow`
- **Class Doc**: ❌ NO DOCUMENTATION
- **Methods**:
  - Constructor - ❌ NO DOCUMENTATION
  - `getDate()` - ❌ NO DOCUMENTATION
  - `getDescription()` - ❌ NO DOCUMENTATION
  - `getAmount()` - ❌ NO DOCUMENTATION
  - `getCategory()` - ❌ NO DOCUMENTATION

### ❌ MonthlyTrend.java
**Class**: `MonthlyTrend`
- **Class Doc**: ❌ NO DOCUMENTATION
- **Methods**:
  - Constructor - ❌ NO DOCUMENTATION
  - `getMonth()` - ❌ NO DOCUMENTATION
  - `getIncome()` - ❌ NO DOCUMENTATION
  - `getExpense()` - ❌ NO DOCUMENTATION
  - `getNet()` - ❌ NO DOCUMENTATION
  - `isPositiveNet()` - ❌ NO DOCUMENTATION
  - `getLabel()` - ❌ NO DOCUMENTATION

### ❌ Transaction.java
**Class**: `Transaction`
- **Class Doc**: ❌ NO DOCUMENTATION
- **Methods**:
  - Constructor - ❌ NO DOCUMENTATION
  - `getId()` - ❌ NO DOCUMENTATION
  - `getDate()` - ❌ NO DOCUMENTATION
  - `getDescription()` - ❌ NO DOCUMENTATION
  - `getAmount()` - ❌ NO DOCUMENTATION
  - `getCategory()` - ❌ NO DOCUMENTATION
  - `isExpense()` - ❌ NO DOCUMENTATION
  - `isIncome()` - ❌ NO DOCUMENTATION

### ❌ UnusualTransaction.java
**Class**: `UnusualTransaction`
- **Class Doc**: ❌ NO DOCUMENTATION
- **Methods**:
  - Constructor - ❌ NO DOCUMENTATION
  - `getTransaction()` - ❌ NO DOCUMENTATION
  - `getAbsoluteAmount()` - ❌ NO DOCUMENTATION
  - `getThreshold()` - ❌ NO DOCUMENTATION

---

## 8. REPOSITORY DIRECTORY
**Path**: `repository/`

### ✅ AuditLogRepository.java
**Class**: `AuditLogRepository` (interface)
- **Class Doc**: ❌ NO DOCUMENTATION
- **Methods**:
  - `findAllByOrderByCreatedAtDesc(...)` - ❌ NO DOCUMENTATION
  - `findByEventTypeOrderByCreatedAtDesc(...)` - ❌ NO DOCUMENTATION
  - `findRecentActivity(...)` - ❌ NO DOCUMENTATION
  - `countByEventType(...)` - ❌ NO DOCUMENTATION

### ❌ BudgetRepository.java
**Class**: `BudgetRepository` (interface)
- **Class Doc**: ❌ NO DOCUMENTATION
- **Methods**:
  - `findByCategory(...)` - ❌ NO DOCUMENTATION
  - `findByEnabledTrueOrderByCategoryAsc()` - ❌ NO DOCUMENTATION
  - `existsByCategory(...)` - ❌ NO DOCUMENTATION

### ✅ CategoryRuleRepository.java
**Class**: `CategoryRuleRepository` (interface)
- **Class Doc**: ✅ YES - "Repository for persistent rule storage and retrieval."
- **Methods**:
  - `findByEnabledTrueOrderByPriorityDesc()` - ❌ NO DOCUMENTATION
  - `findByIsDefaultFalseOrderByCreatedAtDesc()` - ❌ NO DOCUMENTATION
  - `findByIsDefaultTrueOrderByPriorityDesc()` - ❌ NO DOCUMENTATION

### ❌ ProfileRepository.java
**Class**: `ProfileRepository` (interface)
- **Class Doc**: ❌ NO DOCUMENTATION

### ✅ TransactionRepository.java
**Class**: `TransactionRepository` (interface)
- **Class Doc**: ✅ YES - "Repository for transaction persistence in local H2 database."
- **Methods**:
  - `findAllByOrderByDateDescIdDesc()` - ❌ NO DOCUMENTATION
  - `findByDateBetweenOrderByDateDescIdDesc(...)` - ❌ NO DOCUMENTATION
  - `findAllExpenses()` - ❌ NO DOCUMENTATION
  - `findUncategorized()` - ❌ NO DOCUMENTATION
  - `findDistinctUncategorizedDescriptions()` - ❌ NO DOCUMENTATION
  - `countByCategory(...)` - ❌ NO DOCUMENTATION
  - `findRecent(...)` - ❌ NO DOCUMENTATION

### ❌ UploadedFileRepository.java
**Class**: `UploadedFileRepository` (interface)
- **Class Doc**: ❌ NO DOCUMENTATION
- **Methods**:
  - `findAllByOrderByUploadedAtDesc()` - ❌ NO DOCUMENTATION

---

## 9. SERVICE DIRECTORY
**Path**: `service/`

### ✅ AuditService.java
**Class**: `AuditService`
- **Class Doc**: ✅ YES - "Event-driven audit service recording activity to append-only log."
- **Methods**:
  - `onTransactionEvent(...)` - ❌ NO DOCUMENTATION
  - `onBudgetEvent(...)` - ❌ NO DOCUMENTATION
  - `getRecentActivity(...)` - ❌ NO DOCUMENTATION
  - `getActivitySince(...)` - ❌ NO DOCUMENTATION
  - `countByEventType(...)` - ❌ NO DOCUMENTATION
  - `logCustomEvent(...)` - ❌ NO DOCUMENTATION
  - `formatTransactionDetails(...)` - ❌ NO DOCUMENTATION

### ✅ BudgetService.java
**Class**: `BudgetService`
- **Class Doc**: ✅ YES - "Budget management service with per-category spending limits and alerts."
- **Methods**:
  - `createBudget(...)` - ❌ NO DOCUMENTATION
  - `updateBudget(...)` - ❌ NO DOCUMENTATION
  - `deleteBudget(...)` - ❌ NO DOCUMENTATION
  - `getAllBudgets()` - ❌ NO DOCUMENTATION
  - `getBudgetStatuses()` - ✅ YES - "Computes real-time budget status for all enabled budgets against current month spending..."
  - `checkBudgetForCategory(...)` - ✅ YES - "Check budget after a transaction and publish events if thresholds are crossed."
  - `getCurrentMonthSpendByCategory()` - ❌ NO DOCUMENTATION
  - `publishBudgetAlert(...)` - ❌ NO DOCUMENTATION

### ❌ CsvImportService.java
**Class**: `CsvImportService`
- **Class Doc**: ❌ NO DOCUMENTATION
- **Methods**:
  - `parse(MultipartFile)` - ❌ NO DOCUMENTATION
  - `parse(InputStream)` - ❌ NO DOCUMENTATION
  - `parse(BufferedReader)` - ❌ NO DOCUMENTATION
  - `toHeaderIndexMap(...)` - ❌ NO DOCUMENTATION
  - `getRequiredIndex(...)` - ❌ NO DOCUMENTATION
  - `readCell(...)` - ❌ NO DOCUMENTATION
  - `parseDate(...)` - ❌ NO DOCUMENTATION
  - `parseAmount(...)` - ❌ NO DOCUMENTATION
  - `splitCsvLine(...)` - ❌ NO DOCUMENTATION

### ✅ FinanceTrackerService.java
**Class**: `FinanceTrackerService`
- **Class Doc**: ✅ YES - "Core orchestrator for transaction management, CSV import, and analytics."
- **Methods**:
  - `addManualTransaction(...)` - ❌ NO DOCUMENTATION
  - `updateTransaction(...)` - ❌ NO DOCUMENTATION
  - `deleteTransaction(...)` - ❌ NO DOCUMENTATION
  - `importFromCsv(MultipartFile, char[])` - ❌ NO DOCUMENTATION
  - `importFromCsv(MultipartFile)` - ✅ YES - "Backward-compatible: import without passphrase"
  - `importSampleData()` - ❌ NO DOCUMENTATION
  - `getTransactions()` - ❌ NO DOCUMENTATION
  - `getTransactions(LocalDate, LocalDate)` - ❌ NO DOCUMENTATION
  - `getTotalIncome()` - ❌ NO DOCUMENTATION
  - `getTotalExpense()` - ❌ NO DOCUMENTATION
  - `getNetFlow()` - ❌ NO DOCUMENTATION
  - `getTransactionCount()` - ❌ NO DOCUMENTATION
  - `clearTransactions()` - ❌ NO DOCUMENTATION
  - `getMonthlyTrends()` - ❌ NO DOCUMENTATION
  - `getTopCategories(...)` - ❌ NO DOCUMENTATION
  - `getCurrentMonthCategorySpend()` - ❌ NO DOCUMENTATION
  - `getSavingsRatePercent()` - ✅ YES - "Savings rate = (Income - Expense) / Income × 100 (B7)..."
  - `getSavingsRateLevel()` - ❌ NO DOCUMENTATION
  - `detectUnusualTransactions()` - ❌ NO DOCUMENTATION
  - `detectRecurringTransactions()` - ❌ NO DOCUMENTATION
  - `getCustomRules()` - ❌ NO DOCUMENTATION
  - `getDefaultRules()` - ❌ NO DOCUMENTATION
  - `canUndo()` - ❌ NO DOCUMENTATION
  - `getLastActionDescription()` - ❌ NO DOCUMENTATION

### ✅ RuleEngineService.java
**Class**: `RuleEngineService`
- **Class Doc**: ✅ YES - "Deterministic rule engine for transaction categorization."
- **Methods**:
  - `categorize(...)` - ✅ YES - "Categorises a transaction description using the deterministic rule engine..."
  - `matchLength(...)` - ✅ YES - "Returns the match length if the rule matches the haystack..."
  - `addCustomRule(...)` (multiple overloads) - ❌ NO DOCUMENTATION
  - `deleteRule(...)` - ❌ NO DOCUMENTATION
  - `toggleRule(...)` - ❌ NO DOCUMENTATION
  - `getCustomRules()` - ❌ NO DOCUMENTATION
  - `getDefaultRules()` - ❌ NO DOCUMENTATION
  - `getAllEnabledRules()` - ❌ NO DOCUMENTATION
  - `suggestRules()` - ❌ NO DOCUMENTATION

### ✅ ScheduledAnalyticsService.java
**Class**: `ScheduledAnalyticsService`
- **Class Doc**: ✅ YES - "Scheduled analytics service for proactive monitoring of budgets and anomalies."
- **Methods**:
  - `checkBudgetAlerts()` - ✅ YES - "Runs every hour — checks all budgets for threshold violations."
  - `dailySpendingSummary()` - ✅ YES - "Runs daily at midnight — generates a daily spending summary audit entry."
  - `detectAnomalies()` - ✅ YES - "Runs every 6 hours — detects new spending anomalies."

### ✅ StorageService.java (Interface)
**Class**: `StorageService`
- **Class Doc**: ✅ YES - "Service for offline file storage and optional passphrase encryption."
- **Methods**:
  - `saveCsv(...)` - ✅ YES - "Saves an uploaded CSV file to the local uploads directory."
  - `listUploads()` - ✅ YES - "Lists all uploaded files in the uploads directory."
  - `readUpload(...)` - ✅ YES - "Reads an uploaded file, decrypting if a passphrase is provided."
  - `encryptDatabase(...)` - ✅ YES - "Encrypts the H2 database backup file with the given passphrase..."
  - `decryptDatabase(...)` - ✅ YES - "Decrypts a previously encrypted database backup and restores it..."

### ✅ StorageServiceImpl.java
**Class**: `StorageServiceImpl`
- **Class Doc**: ✅ YES - "Offline file persistence with optional AES-256 encryption."
- **Methods**:
  - Constructor - ❌ NO DOCUMENTATION
  - `saveCsv(...)` - ❌ NO DOCUMENTATION
  - `listUploads()` - ❌ NO DOCUMENTATION
  - `readUpload(...)` - ❌ NO DOCUMENTATION
  - `encryptDatabase(...)` - ❌ NO DOCUMENTATION
  - `decryptDatabase(...)` - ❌ NO DOCUMENTATION
  - `sanitizeFileName(...)` - ❌ NO DOCUMENTATION
  - `ensureDirectoryExists(...)` - ❌ NO DOCUMENTATION
  - `resolveDbFile()` - ❌ NO DOCUMENTATION

---

## 10. UTIL DIRECTORY
**Path**: `util/`

### ✅ EncryptionUtil.java
**Class**: `EncryptionUtil`
- **Class Doc**: ✅ YES - "Offline passphrase-based encryption using PBKDF2 + AES-256-GCM."
- **Methods**:
  - `deriveKey(...)` - ✅ YES - "Derives a 256-bit AES key from a passphrase and salt using PBKDF2..."
  - `encrypt(...)` - ✅ YES - "Encrypts plainBytes with AES-256-GCM. Output format: [16-byte salt][12-byte IV][ciphertext+tag]"
  - `decrypt(...)` - ✅ YES - "Decrypts data produced by encrypt(). Reads the prepended salt and IV..."
  - `encryptStream(...)` - ❌ NO DOCUMENTATION
  - `decryptStream(...)` - ❌ NO DOCUMENTATION
  - `randomBytes(...)` - ❌ NO DOCUMENTATION

---

## Summary Statistics

### By Category
| Category | Total Files | Fully Documented | Partially Documented | Not Documented |
|----------|------------|------------------|----------------------|-----------------|
| Config | 4 | 0 | 4 | 0 |
| Controller | 2 | 0 | 2 | 0 |
| DTO | 11 | 11 | 0 | 0 |
| Entity | 6 | 0 | 6 | 0 |
| Exception | 3 | 1 | 2 | 0 |
| Event | 2 | 2 | 0 | 0 |
| Model | 5 | 0 | 0 | 5 |
| Repository | 6 | 1 | 2 | 3 |
| Service | 8 | 1 | 6 | 1 |
| Util | 1 | 1 | 0 | 0 |
| **TOTAL** | **48** | **17** | **22** | **9** |

### Method Documentation Status
- **Methods with JavaDoc/Comments**: ~85 methods
- **Methods WITHOUT any documentation**: ~145 methods
- **Overall coverage**: ~37%

---

## Files Requiring Immediate Documentation

### Priority 1: No Class Documentation
1. `DashboardController.java` - 13 methods need class + method documentation
2. `CsvImportService.java` - 9 methods need class + method documentation
3. Model classes (5 files) - 23 total methods need documentation:
   - `Transaction.java` (8 methods)
   - `CategoryRule.java` (3 methods)
   - `MonthlyTrend.java` (7 methods)
   - `CsvTransactionRow.java` (5 methods)
   - `UnusualTransaction.java` (3 methods)

### Priority 2: Model-Only Documentation (Missing Method Docs)
4. `Config` directory - 4 files, 6 methods missing documentation
5. `Entity` directory - 6 files with getters/setters missing documentation
6. `Repository` interfaces - 6 files with query methods missing documentation
7. `Service` directory - 8 files with many methods missing documentation

### Priority 3: Getter/Setter Methods
- Entity classes have ~40 getter/setter methods without documentation
- Consider using IDE generation or batch documentation for these

---

## Recommendations

1. **Add 1-liner JavaDoc comments to ALL public methods** across the codebase
2. **Document getter/setter methods** with brief descriptions of what they return/set
3. **Document repository query methods** explaining their filter/sort logic
4. **Prioritize service methods** - these are complex and benefit from documentation
5. **Complete model classes** - add class-level documentation and method 1-liners
6. **Controller methods** - keep OpenAPI annotations but add JavaDoc as well

---

## Legend
- ✅ YES = Has JavaDoc or inline documentation
- ❌ NO = Missing documentation
- ⚠️ HAS ANNOTATIONS = Has OpenAPI/Spring annotations but no JavaDoc
