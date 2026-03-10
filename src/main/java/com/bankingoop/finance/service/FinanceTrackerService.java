package com.bankingoop.finance.service;

import com.bankingoop.finance.dto.*;
import com.bankingoop.finance.entity.TransactionEntity;
import com.bankingoop.finance.entity.UploadedFileEntity;
import com.bankingoop.finance.event.TransactionEvent;
import com.bankingoop.finance.model.CsvTransactionRow;
import com.bankingoop.finance.repository.TransactionRepository;
import com.bankingoop.finance.repository.UploadedFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core orchestrator for transaction management, CSV import, and analytics.
 */
@Service
public class FinanceTrackerService {

    private static final Logger log = LoggerFactory.getLogger(FinanceTrackerService.class);

    private final RuleEngineService ruleEngineService;
    private final CsvImportService csvImportService;
    private final TransactionRepository transactionRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final StorageService storageService;
    private final ApplicationEventPublisher eventPublisher;
    private final BudgetService budgetService;

    // Undo buffer: stores the IDs of last batch operation for single-step undo (E23)
    private List<Long> lastAddedIds = new ArrayList<>();
    private String lastActionDescription = "";

    /** Initializes finance tracker with all required service and repository dependencies. */
    public FinanceTrackerService(RuleEngineService ruleEngineService,
                                    CsvImportService csvImportService,
                                    TransactionRepository transactionRepository,
                                    UploadedFileRepository uploadedFileRepository,
                                    StorageService storageService,
                                    ApplicationEventPublisher eventPublisher,
                                    BudgetService budgetService) {
        this.ruleEngineService = ruleEngineService;
        this.csvImportService = csvImportService;
        this.transactionRepository = transactionRepository;
        this.uploadedFileRepository = uploadedFileRepository;
        this.storageService = storageService;
        this.eventPublisher = eventPublisher;
        this.budgetService = budgetService;
    }

    /** Creates a new transaction with automatic categorization and cache invalidation. */
    @Transactional
    @CacheEvict(value = {"monthlyTrends", "topCategories", "savingsRate", "budgetStatuses", "recurringTransactions"}, allEntries = true)
    public TransactionDto addManualTransaction(LocalDate date, String description,
                                               BigDecimal amount, String category) {
        if (date == null) throw new IllegalArgumentException("Date is required.");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description is required.");
        if (amount == null) throw new IllegalArgumentException("Amount is required.");

        RuleEngineService.MatchResult match = ruleEngineService.categorize(description, amount, category);

        TransactionEntity entity = new TransactionEntity();
        entity.setDate(date);
        entity.setDescription(description.trim());
        entity.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        entity.setCategory(match.category());
        entity.setMatchedRuleId(match.matchedRuleId());

        TransactionEntity saved = transactionRepository.save(entity);
        lastAddedIds = List.of(saved.getId());
        lastActionDescription = "Added transaction: " + description;
        log.info("Added transaction #{}: {} {} → {}", saved.getId(), date, description, match.category());

        // Publish event for audit logging and budget checks
        eventPublisher.publishEvent(new TransactionEvent(this, TransactionEvent.Action.CREATED,
                saved.getId(), description, match.category(), amount));
        budgetService.checkBudgetForCategory(match.category());

        return TransactionDto.from(saved);
    }

    /** Updates a transaction's fields and reapplies rule-based categorization. */
    @Transactional
    @CacheEvict(value = {"monthlyTrends", "topCategories", "savingsRate", "budgetStatuses", "recurringTransactions"}, allEntries = true)
    public TransactionDto updateTransaction(Long id, LocalDate date, String description,
                                            BigDecimal amount, String category) {
        TransactionEntity entity = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));

        if (date != null) entity.setDate(date);
        if (description != null && !description.isBlank()) entity.setDescription(description.trim());
        if (amount != null) entity.setAmount(amount.setScale(2, RoundingMode.HALF_UP));

        // Re-categorize if description or explicit category changed
        RuleEngineService.MatchResult match = ruleEngineService.categorize(
                entity.getDescription(), entity.getAmount(), category);
        entity.setCategory(match.category());
        entity.setMatchedRuleId(match.matchedRuleId());

        TransactionEntity saved = transactionRepository.save(entity);
        log.info("Updated transaction #{}", id);

        eventPublisher.publishEvent(new TransactionEvent(this, TransactionEvent.Action.UPDATED,
                saved.getId(), saved.getDescription(), saved.getCategory(), saved.getAmount()));
        budgetService.checkBudgetForCategory(saved.getCategory());

        return TransactionDto.from(saved);
    }

    /** Deletes a transaction and publishes deletion event for audit logging. */
    @Transactional
    @CacheEvict(value = {"monthlyTrends", "topCategories", "savingsRate", "budgetStatuses", "recurringTransactions"}, allEntries = true)
    public void deleteTransaction(Long id) {
        eventPublisher.publishEvent(new TransactionEvent(this, TransactionEvent.Action.DELETED,
                id, null, null, null));
        transactionRepository.deleteById(id);
        log.info("Deleted transaction #{}", id);
    }

    /** Imports transactions from CSV with optional encryption and audit recording. */
    @Transactional
    @CacheEvict(value = {"monthlyTrends", "topCategories", "savingsRate", "budgetStatuses", "recurringTransactions"}, allEntries = true)
    public int importFromCsv(MultipartFile file, char[] passphrase) throws IOException, GeneralSecurityException {
        // Read file into byte array once to avoid consuming the stream multiple times
        byte[] fileBytes = file.getBytes();
        
        // Parse CSV from byte array
        List<CsvTransactionRow> rows = csvImportService.parseFromBytes(fileBytes);

        // Save uploaded file to local storage (optionally encrypted)
        Path storedPath = storageService.saveCsvFromBytes(fileBytes, file.getOriginalFilename(), passphrase);

        List<Long> importedIds = new ArrayList<>();
        for (CsvTransactionRow row : rows) {
            TransactionDto dto = addManualTransaction(row.getDate(), row.getDescription(),
                    row.getAmount(), row.getCategory());
            importedIds.add(dto.id());
        }

        // Record upload in audit table
        UploadedFileEntity uploadRecord = new UploadedFileEntity();
        uploadRecord.setOriginalName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.csv");
        uploadRecord.setStoredPath(storedPath.toString());
        uploadRecord.setEncrypted(passphrase != null && passphrase.length > 0);
        uploadRecord.setRowCount(rows.size());
        uploadedFileRepository.save(uploadRecord);

        lastAddedIds = importedIds;
        lastActionDescription = "Imported " + rows.size() + " transactions from CSV";
        log.info("Imported {} transactions from CSV (stored: {})", rows.size(), storedPath.getFileName());

        eventPublisher.publishEvent(new TransactionEvent(this, TransactionEvent.Action.IMPORTED,
                null, "CSV import: " + rows.size() + " transactions", null, null));

        return rows.size();
    }

    /** Backward-compatible CSV import without passphrase encryption. */
    @Transactional
    @CacheEvict(value = {"monthlyTrends", "topCategories", "savingsRate", "budgetStatuses", "recurringTransactions"}, allEntries = true)
    public int importFromCsv(MultipartFile file) throws IOException {
        try {
            return importFromCsv(file, null);
        } catch (GeneralSecurityException e) {
            throw new IOException("Encryption error during import", e);
        }
    }

    /** Loads demo transactions from sample-transactions.csv resource file. */
    @Transactional
    @CacheEvict(value = {"monthlyTrends", "topCategories", "savingsRate", "budgetStatuses", "recurringTransactions"}, allEntries = true)
    public int importSampleData() throws IOException {
        ClassPathResource resource = new ClassPathResource("sample-transactions.csv");
        try (InputStream inputStream = resource.getInputStream()) {
            List<CsvTransactionRow> rows = csvImportService.parse(inputStream);
            List<Long> importedIds = new ArrayList<>();
            for (CsvTransactionRow row : rows) {
                TransactionDto dto = addManualTransaction(row.getDate(), row.getDescription(),
                        row.getAmount(), row.getCategory());
                importedIds.add(dto.id());
            }
            lastAddedIds = importedIds;
            lastActionDescription = "Loaded " + rows.size() + " sample transactions";
            log.info("Imported {} sample transactions", rows.size());
            return rows.size();
        }
    }

    /** Retrieves all transactions ordered by date (newest first) and ID. */
    public List<TransactionDto> getTransactions() {
        return transactionRepository.findAllByOrderByDateDescIdDesc()
                .stream().map(TransactionDto::from).toList();
    }

    /** Retrieves transactions within date range with optional filtering. */
    public List<TransactionDto> getTransactions(LocalDate from, LocalDate to) {
        if (from == null && to == null) return getTransactions();
        LocalDate start = from != null ? from : LocalDate.of(2000, 1, 1);
        LocalDate end = to != null ? to : LocalDate.of(2099, 12, 31);
        return transactionRepository.findByDateBetweenOrderByDateDescIdDesc(start, end)
                .stream().map(TransactionDto::from).toList();
    }

    /** Calculates sum of all positive (income) transactions. */
    public BigDecimal getTotalIncome() {
        return transactionRepository.findAll().stream()
                .filter(TransactionEntity::isIncome)
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Calculates absolute sum of all negative (expense) transactions. */
    public BigDecimal getTotalExpense() {
        return transactionRepository.findAll().stream()
                .filter(TransactionEntity::isExpense)
                .map(t -> t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Calculates net cash flow (income minus total expenses). */
    public BigDecimal getNetFlow() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    /** Returns total number of transactions in the system. */
    public int getTransactionCount() {
        return (int) transactionRepository.count();
    }

    /** Deletes all transactions and clears undo buffer. */
    @Transactional
    @CacheEvict(value = {"monthlyTrends", "topCategories", "savingsRate", "budgetStatuses", "recurringTransactions"}, allEntries = true)
    public int clearTransactions() {
        int count = getTransactionCount();
        transactionRepository.deleteAll();
        lastAddedIds = List.of();
        lastActionDescription = "";
        log.info("Cleared {} transactions", count);

        eventPublisher.publishEvent(new TransactionEvent(this, TransactionEvent.Action.CLEARED,
                null, "Cleared " + count + " transactions", null, null));

        return count;
    }

    /** Computes monthly income/expense trends with MoM delta and rolling average. */
    @Cacheable("monthlyTrends")
    public List<MonthlyTrendDto> getMonthlyTrends() {
        List<TransactionEntity> all = transactionRepository.findAll();
        Map<YearMonth, BigDecimal> incomeByMonth = new TreeMap<>();
        Map<YearMonth, BigDecimal> expenseByMonth = new TreeMap<>();

        for (TransactionEntity tx : all) {
            YearMonth ym = YearMonth.from(tx.getDate());
            if (tx.isIncome()) {
                incomeByMonth.merge(ym, tx.getAmount(), BigDecimal::add);
            } else {
                expenseByMonth.merge(ym, tx.getAmount().abs(), BigDecimal::add);
            }
        }

        Set<YearMonth> allMonths = new TreeSet<>();
        allMonths.addAll(incomeByMonth.keySet());
        allMonths.addAll(expenseByMonth.keySet());

        List<YearMonth> monthList = new ArrayList<>(allMonths);
        List<MonthlyTrendDto> result = new ArrayList<>();

        for (int i = 0; i < monthList.size(); i++) {
            YearMonth ym = monthList.get(i);
            BigDecimal income = incomeByMonth.getOrDefault(ym, BigDecimal.ZERO);
            BigDecimal expense = expenseByMonth.getOrDefault(ym, BigDecimal.ZERO);

            // Month-over-month delta (B4)
            BigDecimal momDelta = null;
            if (i > 0) {
                BigDecimal prevExpense = expenseByMonth.getOrDefault(monthList.get(i - 1), BigDecimal.ZERO);
                if (prevExpense.compareTo(BigDecimal.ZERO) != 0) {
                    momDelta = expense.subtract(prevExpense)
                            .divide(prevExpense, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(1, RoundingMode.HALF_UP);
                }
            }

            // 3-month rolling average of expenses (B6)
            BigDecimal rollingAvg = BigDecimal.ZERO;
            int rollingCount = 0;
            for (int j = Math.max(0, i - 2); j <= i; j++) {
                rollingAvg = rollingAvg.add(expenseByMonth.getOrDefault(monthList.get(j), BigDecimal.ZERO));
                rollingCount++;
            }
            rollingAvg = rollingAvg.divide(BigDecimal.valueOf(rollingCount), 2, RoundingMode.HALF_UP);

            result.add(MonthlyTrendDto.of(ym, income, expense, momDelta, rollingAvg));
        }

        // Return newest first
        result.sort(Comparator.comparing(MonthlyTrendDto::month).reversed());
        return result;
    }

    /** Retrieves top N spending categories by total amount. */
    @Cacheable(value = "topCategories", key = "#limit")
    public List<CategorySpendDto> getTopCategories(int limit) {
        return transactionRepository.findAll().stream()
                .filter(TransactionEntity::isExpense)
                .collect(Collectors.groupingBy(
                        TransactionEntity::getCategory,
                        Collectors.toList()))
                .entrySet().stream()
                .map(e -> new CategorySpendDto(
                        e.getKey(),
                        e.getValue().stream().map(t -> t.getAmount().abs())
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        e.getValue().size()))
                .sorted(Comparator.comparing(CategorySpendDto::amount).reversed())
                .limit(limit)
                .toList();
    }

    /** Returns current month spending grouped by category in descending order. */
    public Map<String, BigDecimal> getCurrentMonthCategorySpend() {
        YearMonth currentMonth = YearMonth.now();
        return transactionRepository.findAll().stream()
                .filter(TransactionEntity::isExpense)
                .filter(t -> YearMonth.from(t.getDate()).equals(currentMonth))
                .collect(Collectors.groupingBy(
                        TransactionEntity::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, t -> t.getAmount().abs(), BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    /** Calculates savings rate as percentage of income, returns null if no income. */
    @Cacheable("savingsRate")
    public BigDecimal getSavingsRatePercent() {
        BigDecimal income = getTotalIncome();
        if (income.compareTo(BigDecimal.ZERO) == 0) return null;
        return income.subtract(getTotalExpense())
                .divide(income, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    /** Returns savings rate classification: good (≥20%), warning (10-20%), danger (<10%). */
    public String getSavingsRateLevel() {
        BigDecimal rate = getSavingsRatePercent();
        if (rate == null) return "neutral";
        if (rate.compareTo(BigDecimal.valueOf(20)) >= 0) return "good";
        if (rate.compareTo(BigDecimal.valueOf(10)) >= 0) return "warning";
        return "danger";
    }

    /** Identifies outlier transactions using statistical analysis (mean + 2*stddev). */
    public List<UnusualTransactionDto> detectUnusualTransactions() {
        List<TransactionEntity> expenses = transactionRepository.findAllExpenses();
        if (expenses.size() < 5) return List.of();

        List<Double> values = expenses.stream()
                .map(t -> t.getAmount().abs().doubleValue()).toList();

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        double threshold = stdDev == 0.0 ? mean * 1.5 : mean + (2 * stdDev);

        BigDecimal thresholdValue = BigDecimal.valueOf(threshold).setScale(2, RoundingMode.HALF_UP);

        return expenses.stream()
                .filter(t -> t.getAmount().abs().compareTo(thresholdValue) >= 0
                        && t.getAmount().abs().compareTo(BigDecimal.valueOf(20)) >= 0)
                .sorted(Comparator.comparing((TransactionEntity t) -> t.getAmount().abs()).reversed())
                .limit(10)
                .map(t -> new UnusualTransactionDto(
                        TransactionDto.from(t), t.getAmount().abs(), thresholdValue))
                .toList();
    }

    /** Detects recurring transactions by matching descriptions across multiple months. */
    @Cacheable("recurringTransactions")
    public List<RecurringTransactionDto> detectRecurringTransactions() {
        List<TransactionEntity> all = transactionRepository.findAll();

        // Group by normalised description (lowercase, trimmed)
        Map<String, List<TransactionEntity>> groups = all.stream()
                .collect(Collectors.groupingBy(t ->
                        t.getDescription().trim().toLowerCase(Locale.ROOT)));

        List<RecurringTransactionDto> result = new ArrayList<>();
        for (Map.Entry<String, List<TransactionEntity>> entry : groups.entrySet()) {
            List<TransactionEntity> txns = entry.getValue();
            if (txns.size() < 2) continue;

            // Check distinct months
            long distinctMonths = txns.stream()
                    .map(t -> YearMonth.from(t.getDate()))
                    .distinct().count();
            if (distinctMonths < 2) continue;

            // Check amount consistency (within 5%)
            BigDecimal avgAmount = txns.stream()
                    .map(t -> t.getAmount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(txns.size()), 2, RoundingMode.HALF_UP);

            boolean consistent = txns.stream().allMatch(t -> {
                BigDecimal diff = t.getAmount().abs().subtract(avgAmount).abs();
                return avgAmount.compareTo(BigDecimal.ZERO) == 0 ||
                        diff.divide(avgAmount, 4, RoundingMode.HALF_UP)
                                .compareTo(BigDecimal.valueOf(0.05)) <= 0;
            });

            if (consistent) {
                String freq = distinctMonths >= txns.size() ? "Monthly" : "Recurring";
                result.add(new RecurringTransactionDto(
                        txns.get(0).getDescription(),
                        avgAmount,
                        txns.size(),
                        freq));
            }
        }

        result.sort(Comparator.comparing(RecurringTransactionDto::occurrenceCount).reversed());
        return result;
    }

    /** Reverts the last batch operation (manual add/import) and returns result message. */
    @Transactional
    public String undoLastAction() {
        if (lastAddedIds.isEmpty()) {
            return null;
        }
        transactionRepository.deleteAllById(lastAddedIds);
        String msg = "Undone: " + lastActionDescription + " (" + lastAddedIds.size() + " transactions removed)";
        log.info(msg);
        lastAddedIds = List.of();
        lastActionDescription = "";
        return msg;
    }

    /** Returns true if there are transactions available to undo. */
    public boolean canUndo() {
        return !lastAddedIds.isEmpty();
    }

    /** Returns description of the last action performed for undo context. */
    public String getLastActionDescription() {
        return lastActionDescription;
    }

    /** Delegates to rule engine to create a keyword-based categorization rule. */
    public void addRule(String keyword, String category) {
        ruleEngineService.addCustomRule(keyword, category);
    }

    /** Delegates to rule engine to create a pattern-based categorization rule with priority. */
    public void addRule(String patternType, String pattern, String category, int priority) {
        ruleEngineService.addCustomRule(patternType, pattern, category, priority);
    }

    /** Delegates to retrieve user-created categorization rules in reverse creation order. */
    public List<CategoryRuleDto> getCustomRules() {
        return ruleEngineService.getCustomRules();
    }

    /** Delegates to retrieve built-in categorization rules ordered by priority. */
    public List<CategoryRuleDto> getDefaultRules() {
        return ruleEngineService.getDefaultRules();
    }

    /** Delegates to generate auto-suggestions for new rules from uncategorized transactions. */
    public List<RuleSuggestionDto> suggestRules() {
        return ruleEngineService.suggestRules();
    }

    /** Recategorizes all transactions using current rules and updates their categories. */
    @Transactional
    @CacheEvict(value = {"monthlyTrends", "topCategories", "savingsRate", "budgetStatuses", "recurringTransactions"}, allEntries = true)
    public int recategorizeAllTransactions() {
        List<TransactionEntity> allTransactions = transactionRepository.findAll();
        int updatedCount = 0;

        for (TransactionEntity entity : allTransactions) {
            RuleEngineService.MatchResult match = ruleEngineService.categorize(
                    entity.getDescription(), entity.getAmount(), null);
            
            // Only update if category changed
            if (!entity.getCategory().equals(match.category())) {
                entity.setCategory(match.category());
                entity.setMatchedRuleId(match.matchedRuleId());
                transactionRepository.save(entity);
                updatedCount++;
            }
        }

        lastActionDescription = "Recategorized " + updatedCount + " transactions";
        log.info("Recategorized {} out of {} transactions", updatedCount, allTransactions.size());
        return updatedCount;
    }

    /** Exports transactions within date range to CSV format with proper escaping. */
    public String exportToCsv(LocalDate from, LocalDate to) {
        List<TransactionDto> transactions = getTransactions(from, to);
        StringBuilder sb = new StringBuilder();
        sb.append("date,description,amount,category\n");
        for (TransactionDto tx : transactions) {
            sb.append(tx.date()).append(',');
            sb.append('"').append(tx.description().replace("\"", "\"\"")).append('"').append(',');
            sb.append(tx.amount()).append(',');
            sb.append('"').append(tx.category()).append('"').append('\n');
        }
        return sb.toString();
    }
}
