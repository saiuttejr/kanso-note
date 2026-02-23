package com.bankingoop.finance.service;

import com.bankingoop.finance.model.CategoryRule;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RuleEngineService {
    private final List<CategoryRule> defaultRules = Arrays.asList(
            new CategoryRule("walmart", "Groceries"),
            new CategoryRule("target", "Groceries"),
            new CategoryRule("costco", "Groceries"),
            new CategoryRule("uber", "Transport"),
            new CategoryRule("lyft", "Transport"),
            new CategoryRule("shell", "Fuel"),
            new CategoryRule("chevron", "Fuel"),
            new CategoryRule("electric", "Utilities"),
            new CategoryRule("water bill", "Utilities"),
            new CategoryRule("netflix", "Subscriptions"),
            new CategoryRule("spotify", "Subscriptions"),
            new CategoryRule("rent", "Housing"),
            new CategoryRule("salary", "Income"),
            new CategoryRule("payroll", "Income"),
            new CategoryRule("amazon", "Shopping"),
            new CategoryRule("restaurant", "Dining"),
            new CategoryRule("cafe", "Dining")
    );

    private final List<CategoryRule> customRules = new CopyOnWriteArrayList<>();

    public String categorize(String description, BigDecimal amount, String explicitCategory) {
        if (explicitCategory != null && !explicitCategory.isBlank()) {
            return normalizeCategory(explicitCategory);
        }

        String haystack = description == null ? "" : description.toLowerCase(Locale.ROOT);
        List<CategoryRule> allRules = new ArrayList<>(customRules);
        allRules.addAll(defaultRules);

        for (CategoryRule rule : allRules) {
            String keyword = rule.getKeyword() == null ? "" : rule.getKeyword().toLowerCase(Locale.ROOT);
            if (!keyword.isBlank() && haystack.contains(keyword)) {
                return normalizeCategory(rule.getCategory());
            }
        }

        if (amount != null && amount.compareTo(BigDecimal.ZERO) >= 0) {
            return "Income";
        }

        return "Uncategorized";
    }

    public void addCustomRule(String keyword, String category) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword is required.");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }

        customRules.add(0, new CategoryRule(keyword.trim(), normalizeCategory(category)));
    }

    public List<CategoryRule> getCustomRules() {
        return List.copyOf(customRules);
    }

    public List<CategoryRule> getDefaultRules() {
        return List.copyOf(defaultRules);
    }

    private String normalizeCategory(String category) {
        String cleaned = category.trim().replaceAll("\\s+", " ");
        if (cleaned.isBlank()) {
            return "Uncategorized";
        }

        String[] parts = cleaned.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isBlank()) {
                continue;
            }
            String normalized = part.substring(0, 1).toUpperCase(Locale.ROOT)
                    + part.substring(1).toLowerCase(Locale.ROOT);
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(normalized);
        }
        return builder.toString();
    }
}
