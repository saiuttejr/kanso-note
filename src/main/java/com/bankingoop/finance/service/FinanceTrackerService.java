package com.bankingoop.finance.service;

import com.bankingoop.finance.model.CsvTransactionRow;
import com.bankingoop.finance.model.MonthlyTrend;
import com.bankingoop.finance.model.Transaction;
import com.bankingoop.finance.model.UnusualTransaction;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class FinanceTrackerService {
    private final RuleEngineService ruleEngineService;
    private final CsvImportService csvImportService;
    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public FinanceTrackerService(RuleEngineService ruleEngineService, CsvImportService csvImportService) {
        this.ruleEngineService = ruleEngineService;
        this.csvImportService = csvImportService;
    }

    public Transaction addManualTransaction(LocalDate date, String description, BigDecimal amount, String category) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required.");
        }

        String resolvedCategory = ruleEngineService.categorize(description, amount, category);
        Transaction transaction = new Transaction(
                idCounter.getAndIncrement(),
                date,
                description.trim(),
                amount.setScale(2, RoundingMode.HALF_UP),
                resolvedCategory
        );
        transactions.add(transaction);
        return transaction;
    }

    public int importFromCsv(MultipartFile file) throws IOException {
        List<CsvTransactionRow> rows = csvImportService.parse(file);
        return addImportedRows(rows);
    }

    public int importSampleData() throws IOException {
        ClassPathResource resource = new ClassPathResource("sample-transactions.csv");
        try (InputStream inputStream = resource.getInputStream()) {
            List<CsvTransactionRow> rows = csvImportService.parse(inputStream);
            return addImportedRows(rows);
        }
    }

    public List<Transaction> getTransactions() {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate).reversed()
                        .thenComparing(Comparator.comparing(Transaction::getId).reversed()))
                .toList();
    }

    public BigDecimal getTotalIncome() {
        return transactions.stream()
                .filter(Transaction::isIncome)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpense() {
        return transactions.stream()
                .filter(Transaction::isExpense)
                .map(Transaction::getAmount)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getNetFlow() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    public int getTransactionCount() {
        return transactions.size();
    }

    public List<MonthlyTrend> getMonthlyTrends() {
        Map<YearMonth, BigDecimal> incomeByMonth = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal> expenseByMonth = new LinkedHashMap<>();
        List<YearMonth> months = new ArrayList<>();

        transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .forEach(transaction -> {
                    YearMonth month = YearMonth.from(transaction.getDate());
                    if (!months.contains(month)) {
                        months.add(month);
                    }
                    if (transaction.isIncome()) {
                        incomeByMonth.merge(month, transaction.getAmount(), BigDecimal::add);
                    } else {
                        expenseByMonth.merge(month, transaction.getAmount().abs(), BigDecimal::add);
                    }
                });

        return months.stream()
                .map(month -> new MonthlyTrend(
                        month,
                        incomeByMonth.getOrDefault(month, BigDecimal.ZERO),
                        expenseByMonth.getOrDefault(month, BigDecimal.ZERO)
                ))
                .sorted(Comparator.comparing(MonthlyTrend::getMonth).reversed())
                .toList();
    }

    public Map<String, BigDecimal> getCurrentMonthCategorySpend() {
        YearMonth currentMonth = YearMonth.now();
        return transactions.stream()
                .filter(Transaction::isExpense)
                .filter(t -> YearMonth.from(t.getDate()).equals(currentMonth))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, t -> t.getAmount().abs(), BigDecimal::add)
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    public List<UnusualTransaction> detectUnusualTransactions() {
        List<Transaction> expenses = transactions.stream()
                .filter(Transaction::isExpense)
                .toList();
        if (expenses.size() < 5) {
            return List.of();
        }

        List<Double> values = expenses.stream()
                .map(t -> t.getAmount().abs().doubleValue())
                .toList();

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);

        double threshold = mean + (2 * stdDev);
        if (stdDev == 0.0) {
            threshold = mean * 1.5;
        }

        BigDecimal thresholdValue = BigDecimal.valueOf(threshold).setScale(2, RoundingMode.HALF_UP);

        List<UnusualTransaction> unusual = new ArrayList<>();
        for (Transaction transaction : expenses) {
            BigDecimal absolute = transaction.getAmount().abs();
            if (absolute.compareTo(thresholdValue) >= 0 && absolute.compareTo(BigDecimal.valueOf(20)) >= 0) {
                unusual.add(new UnusualTransaction(transaction, absolute, thresholdValue));
            }
        }

        return unusual.stream()
                .sorted(Comparator.comparing(UnusualTransaction::getAbsoluteAmount).reversed())
                .limit(10)
                .toList();
    }

    public void addRule(String keyword, String category) {
        ruleEngineService.addCustomRule(keyword, category);
    }

    public List<com.bankingoop.finance.model.CategoryRule> getCustomRules() {
        return ruleEngineService.getCustomRules();
    }

    public List<com.bankingoop.finance.model.CategoryRule> getDefaultRules() {
        return ruleEngineService.getDefaultRules();
    }

    private int addImportedRows(List<CsvTransactionRow> rows) {
        int imported = 0;
        for (CsvTransactionRow row : rows) {
            addManualTransaction(row.getDate(), row.getDescription(), row.getAmount(), row.getCategory());
            imported++;
        }
        return imported;
    }
}
