package com.bankingoop.finance.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingoop.finance.dto.CategoryRuleDto;
import com.bankingoop.finance.dto.RuleSuggestionDto;
import com.bankingoop.finance.entity.CategoryRuleEntity;
import com.bankingoop.finance.repository.CategoryRuleRepository;
import com.bankingoop.finance.repository.TransactionRepository;

/**
 * Deterministic rule engine for transaction categorization.
 */
@Service
public class RuleEngineService {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineService.class);

    private final CategoryRuleRepository ruleRepository;
    private final TransactionRepository transactionRepository;

    public RuleEngineService(CategoryRuleRepository ruleRepository,
                             TransactionRepository transactionRepository) {
        this.ruleRepository = ruleRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Categorises a transaction description using the deterministic rule engine.
     *
     * Resolution order:
     *  1. If an explicit category is provided, use it (user override).
     *  2. Collect all enabled rules that match the description.
     *  3. Sort matches: highest priority first, then longest pattern (most specific).
     *  4. Pick the winner. Log the decision.
     *  5. Fallback: positive amount → "Income", otherwise → "Uncategorized".
     *
     * @return a MatchResult containing the chosen category and the matched rule ID (if any).
     */
    public MatchResult categorize(String description, BigDecimal amount, String explicitCategory) {
        if (explicitCategory != null && !explicitCategory.isBlank()) {
            String normalized = normalizeCategory(explicitCategory);
            log.debug("Using explicit category '{}' for '{}'", normalized, description);
            return new MatchResult(normalized, null);
        }

        String haystack = description == null ? "" : description.toLowerCase(Locale.ROOT);
        List<CategoryRuleEntity> enabledRules = ruleRepository.findByEnabledTrueOrderByPriorityDesc();

        // Collect all matching rules with their match length
        List<RuleMatch> matches = new ArrayList<>();
        for (CategoryRuleEntity rule : enabledRules) {
            int matchLength = matchLength(rule, haystack);
            if (matchLength > 0) {
                matches.add(new RuleMatch(rule, matchLength));
                log.debug("Rule #{} ({} '{}') matched '{}' with length {}",
                        rule.getId(), rule.getPatternType(), rule.getPattern(),
                        description, matchLength);
            }
        }

        if (!matches.isEmpty()) {
            // Deterministic conflict resolution: highest priority, then longest match
            matches.sort(Comparator
                    .comparingInt((RuleMatch m) -> -m.rule().getPriority())
                    .thenComparingInt((RuleMatch m) -> -m.matchLength()));

            RuleMatch winner = matches.get(0);
            String category = normalizeCategory(winner.rule().getCategory());

            log.info("Categorised '{}' → '{}' via rule #{} (priority={}, pattern='{}', matchLen={}). {} candidate(s) evaluated.",
                    description, category, winner.rule().getId(),
                    winner.rule().getPriority(), winner.rule().getPattern(),
                    winner.matchLength(), matches.size());

            return new MatchResult(category, winner.rule().getId());
        }

        // Fallback: positive amount treated as income
        if (amount != null && amount.compareTo(BigDecimal.ZERO) >= 0) {
            log.debug("No rule matched '{}'; positive amount → Income", description);
            return new MatchResult("Income", null);
        }

        log.debug("No rule matched '{}'; → Uncategorized", description);
        return new MatchResult("Uncategorized", null);
    }

    /**
     * Returns the match length if the rule matches the haystack, or 0 if no match.
     * KEYWORD: case-insensitive substring match, returns keyword length.
     * REGEX: Java regex find(), returns the length of the matched region.
     */
    private int matchLength(CategoryRuleEntity rule, String haystack) {
        String pattern = rule.getPattern();
        if (pattern == null || pattern.isBlank()) {
            return 0;
        }

        if ("REGEX".equalsIgnoreCase(rule.getPatternType())) {
            try {
                var matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(haystack);
                if (matcher.find()) {
                    return matcher.group().length();
                }
            } catch (PatternSyntaxException e) {
                log.warn("Invalid regex in rule #{}: '{}' — skipping", rule.getId(), pattern);
            }
            return 0;
        }

        // Default: KEYWORD substring match
        String keyword = pattern.toLowerCase(Locale.ROOT);
        if (haystack.contains(keyword)) {
            return keyword.length();
        }
        return 0;
    }

    // --- Rule CRUD ---

    @Transactional
    public CategoryRuleEntity addCustomRule(String patternType, String pattern, String category, int priority) {
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalArgumentException("Pattern is required.");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if ("REGEX".equalsIgnoreCase(patternType)) {
            try {
                Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid regex pattern: " + e.getMessage());
            }
        }

        CategoryRuleEntity entity = new CategoryRuleEntity();
        entity.setPatternType(patternType != null ? patternType.toUpperCase(Locale.ROOT) : "KEYWORD");
        entity.setPattern(pattern.trim());
        entity.setCategory(normalizeCategory(category));
        entity.setPriority(priority);
        entity.setDefault(false);
        entity.setEnabled(true);

        CategoryRuleEntity saved = ruleRepository.save(entity);
        log.info("Added custom rule #{}: {} '{}' → '{}' (priority={})",
                saved.getId(), saved.getPatternType(), saved.getPattern(),
                saved.getCategory(), saved.getPriority());
        return saved;
    }

    /** Backward-compatible: add KEYWORD rule with default priority */
    @Transactional
    public CategoryRuleEntity addCustomRule(String keyword, String category) {
        return addCustomRule("KEYWORD", keyword, category, 10);
    }

    @Transactional
    public void deleteRule(Long ruleId) {
        ruleRepository.deleteById(ruleId);
        log.info("Deleted rule #{}", ruleId);
    }

    @Transactional
    public void toggleRule(Long ruleId, boolean enabled) {
        ruleRepository.findById(ruleId).ifPresent(rule -> {
            rule.setEnabled(enabled);
            ruleRepository.save(rule);
            log.info("Rule #{} enabled={}", ruleId, enabled);
        });
    }

    public List<CategoryRuleDto> getCustomRules() {
        return ruleRepository.findByIsDefaultFalseOrderByCreatedAtDesc()
                .stream().map(CategoryRuleDto::from).toList();
    }

    public List<CategoryRuleDto> getDefaultRules() {
        return ruleRepository.findByIsDefaultTrueOrderByPriorityDesc()
                .stream().map(CategoryRuleDto::from).toList();
    }

    public List<CategoryRuleDto> getAllEnabledRules() {
        return ruleRepository.findByEnabledTrueOrderByPriorityDesc()
                .stream().map(CategoryRuleDto::from).toList();
    }

    // --- Auto-suggest rules from uncategorized transactions ---

    /**
     * Suggests categorization rules from uncategorized transaction descriptions.
     */
    public List<RuleSuggestionDto> suggestRules() {
        List<String> descriptions = transactionRepository.findDistinctUncategorizedDescriptions();
        if (descriptions.isEmpty()) {
            return List.of();
        }

        // Extract first meaningful word (≥3 chars) from each description as a candidate keyword
        Map<String, Integer> keywordCounts = new LinkedHashMap<>();
        for (String desc : descriptions) {
            String keyword = extractKeyword(desc);
            if (keyword != null) {
                keywordCounts.merge(keyword, 1, Integer::sum);
            }
        }

        return keywordCounts.entrySet().stream()
                .filter(e -> e.getValue() >= 1)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(e -> new RuleSuggestionDto(e.getKey(), e.getValue(), ""))
                .toList();
    }

    private String extractKeyword(String description) {
        if (description == null || description.isBlank()) return null;
        String[] words = description.trim().toLowerCase(Locale.ROOT).split("\\s+");
        for (String word : words) {
            String cleaned = word.replaceAll("[^a-z]", "");
            if (cleaned.length() >= 3) {
                return cleaned;
            }
        }
        return null;
    }

    // --- Helpers ---

    String normalizeCategory(String category) {
        String cleaned = category.trim().replaceAll("\\s+", " ");
        if (cleaned.isBlank()) return "Uncategorized";
        String[] parts = cleaned.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(part.substring(0, 1).toUpperCase(Locale.ROOT))
              .append(part.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }

    // --- Inner types ---

    public record MatchResult(String category, Long matchedRuleId) {}
    private record RuleMatch(CategoryRuleEntity rule, int matchLength) {}
}
