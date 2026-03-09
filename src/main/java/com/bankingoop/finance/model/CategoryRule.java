package com.bankingoop.finance.model;

// Rule-based category mapping from keyword to category name for auto-categorization of transactions.
public class CategoryRule {
    private final String keyword;
    private final String category;

    // Constructs a CategoryRule with a keyword and associated category.
    public CategoryRule(String keyword, String category) {
        this.keyword = keyword;
        this.category = category;
    }

    // Returns the keyword to match in transaction descriptions.
    public String getKeyword() {
        return keyword;
    }

    // Returns the category assigned to matching transactions.
    public String getCategory() {
        return category;
    }
}
