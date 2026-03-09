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
 * Supports: core orchestrator — ties together CSV import, rule engine, persistence, and analytics.
 *
 * Design decision — JPA-backed persistence:
 *   All transactions are now stored in the local H2 file database instead of an in-memory
 *   list, so data survives restarts. The service delegates categorisation to RuleEngineService
 *   and file operations to StorageService.
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

    // -----------------------------------------------------------------------
    // Transaction CRUD
    // -----------------------------------------------------------------------

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

    @Transactional
    @CacheEvict(value = {"monthlyTrends", "topCategories", "savingsRate", "budgetStatuses", "recurringTransactions"}, allEntries = true)
    public void deleteTransaction(Long id) {
        eventPublisher.publishEvent(new TransactionEvent(this, TransactionEvent.Action.DELETED,
                id, null, null, null));
        transactionRepository.deleteById(id);
        log.info("Deleted transaction #{}", id);
    }

    // -----------------------------------------------------------------------
    // CSV Import with StorageService integration (A2)
    // -----------------------------------------------------------------------

    @Transactional
    @CacheEvict(value = {"monthlyTrends", "topCategories", "savingsRate", "budgetStatuses", "recurringTransactions"}, allEntries = true)
    public int importFromCsv(MultipartFile file, char[] passphrase) throws IOException, GeneralSecurityException {
        // Save uploaded file to local storage (optionally encrypted)
        Path storedPath = storageService.saveCsv(file, passphrase);

        List<CsvTransactionRow> rows = csvImportService.parse(file);
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

    /** Backward-compatible: import without passphrase */
    @Transactional
    @CacheEvict(value = {"monthlyTrends", "topCategories", "savingsRate", "budgetStatuses", "recurringTransactions"}, allEntries = true)
    public int importFromCsv(MultipartFile file) throws IOException {
        try {
            return importFromCsv(file, null);
        } catch (GeneralSecurityException e) {
            throw new IOException("Encryption error during import", e);
        }
    }

    @Transactional
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

    // -----------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------

    public List<TransactionDto> getTransactions() {
        return transactionRepository.findAllByOrderByDateDescIdDesc()
                .stream().map(TransactionDto::from).toList();
    }

    public List<TransactionDto> getTransactions(LocalDate from, LocalDate to) {
        if (from == null && to == null) return getTransactions();
        LocalDate start = from != null ? from : LocalDate.of(2000, 1, 1);
        LocalDate end = to != null ? to : LocalDate.of(2099, 12, 31);
        return transactionRepository.findByDateBetweenOrderByDateDescIdDesc(start, end)
                .stream().map(TransactionDto::from).toList();
    }

    public BigDecimal getTotalIncome() {
        return transactionRepository.findAll().stream()
                .filter(TransactionEntity::isIncome)
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpense() {
        return transactionRepository.findAll().stream()
                .filter(TransactionEntity::isExpense)
                .map(t -> t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getNetFlow() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    public int getTransactionCount() {
        return (int) transactionRepository.count();
    }

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

    // -----------------------------------------------------------------------
    // Analytics (B4-7): Monthly trends with MoM delta + rolling avg + savings rate
    // -----------------------------------------------------------------------

    /**
     * Supports: monthly aggregation with MoM delta and 3-month rolling average (interview talking point).
     *
     * Design decision — local computation:
     *   We compute everything in Java from the full transaction list rather than
     *   JPQL aggregation. For a single-user app with < 100K transactions this is
     *   fast enough and keeps the code simple and testable.
     */
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

    /** Top-N spending categories across all data (B5) */
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

    /** Current month spending by category (existing feature) */
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

    /**
     * Savings rate = (Income - Expense) / Income × 100 (B7).
     * Returns null if no income. Color levels: ≥20% good, 10-20% warning, <10% danger.
     */
    @Cacheable("savingsRate")
    public BigDecimal getSavingsRatePercent() {
        BigDecimal income = getTotalIncome();
        if (income.compareTo(BigDecimal.ZERO) == 0) return null;
        return income.subtract(getTotalExpense())
                .divide(income, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    public String getSavingsRateLevel() {
        BigDecimal rate = getSavingsRatePercent();
        if (rate == null) return "neutral";
        if (rate.compareTo(BigDecimal.valueOf(20)) >= 0) return "good";
        if (rate.compareTo(BigDecimal.valueOf(10)) >= 0) return "warning";
        return "danger";
    }

    // -----------------------------------------------------------------------
    // Anomaly detection (existing, adapted for JPA)
    // -----------------------------------------------------------------------

    /**
     * Supports: anomaly detection using mean + 2σ (interview talking point).
     *
     * Design decision — statistical threshold:
     *   We flag expenses > mean + 2×stddev. This catches ~2.5% of transactions
     *   in a normal distribution, surfacing genuinely unusual spending.
     *   The $20 floor avoids noise from tiny transactions.
     */
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

    // -----------------------------------------------------------------------
    // Recurring transaction detection (C9)
    // -----------------------------------------------------------------------

    /**
     * Supports: recurring transaction detection (interview talking point).
     *
     * Design decision: group transactions by normalised description + approximate amount
     * (within 5% tolerance). If a description appears in ≥ 2 distinct months with
     * similar amounts, mark it as recurring. This catches rent, salary, subscriptions.
     */
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

    // -----------------------------------------------------------------------
    // Undo (E23)
    // -----------------------------------------------------------------------

    /**
     * Supports: single-step undo of the last add/import action (interview talking point).
     *
     * Design decision: we store only the IDs added by the last action. Undo deletes them.
     * This is simpler than a full command pattern but sufficient for a beginner UX.
     */
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

    public boolean canUndo() {
        return !lastAddedIds.isEmpty();
    }

    public String getLastActionDescription() {
        return lastActionDescription;
    }

    // -----------------------------------------------------------------------
    // Rule delegation
    // -----------------------------------------------------------------------

    public void addRule(String keyword, String category) {
        ruleEngineService.addCustomRule(keyword, category);
    }

    public void addRule(String patternType, String pattern, String category, int priority) {
        ruleEngineService.addCustomRule(patternType, pattern, category, priority);
    }

    public List<CategoryRuleDto> getCustomRules() {
        return ruleEngineService.getCustomRules();
    }

    public List<CategoryRuleDto> getDefaultRules() {
        return ruleEngineService.getDefaultRules();
    }

    public List<RuleSuggestionDto> suggestRules() {
        return ruleEngineService.suggestRules();
    }

    // -----------------------------------------------------------------------
    // Export (C11)
    // -----------------------------------------------------------------------

    /**
     * Exports transactions as CSV string. Supports optional date range filter (C12).
     */
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
