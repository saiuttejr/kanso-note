package com.bankingoop.finance.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.bankingoop.finance.dto.TransactionDto;
import com.bankingoop.finance.service.AuditService;
import com.bankingoop.finance.service.BudgetService;
import com.bankingoop.finance.service.FinanceTrackerService;

// Web controller for HTML dashboard views with transaction, budget, and analytics management.
@Controller
public class DashboardController {

    private final FinanceTrackerService financeTrackerService;
    private final BudgetService budgetService;
    private final AuditService auditService;

    // Constructor injecting dependent services.
    public DashboardController(FinanceTrackerService financeTrackerService,
                               BudgetService budgetService,
                               AuditService auditService) {
        this.financeTrackerService = financeTrackerService;
        this.budgetService = budgetService;
        this.auditService = auditService;
    }

    // Renders the main dashboard view with transactions, trends, budgets, and analytics.
    @GetMapping("/")
    public String dashboard(@RequestParam(value = "message", required = false) String message,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "from", required = false) String from,
                            @RequestParam(value = "to", required = false) String to,
                            Model model) {
        LocalDate fromDate = parseOptionalDate(from);
        LocalDate toDate = parseOptionalDate(to);

        populateModel(model, fromDate, toDate);
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("filterFrom", from != null ? from : "");
        model.addAttribute("filterTo", to != null ? to : "");

        // Undo state (E23)
        model.addAttribute("canUndo", financeTrackerService.canUndo());
        model.addAttribute("lastAction", financeTrackerService.getLastActionDescription());

        // Onboarding state (E20)
        model.addAttribute("isFirstUse", financeTrackerService.getTransactionCount() == 0);

        return "dashboard";
    }

    // Handles manual transaction entry with date parsing and error messaging.
    @PostMapping("/transactions/manual")
    public String addManualTransaction(@RequestParam("date") String date,
                                       @RequestParam("description") String description,
                                       @RequestParam("amount") String amount,
                                       @RequestParam(value = "category", required = false) String category) {
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            BigDecimal parsedAmount = new BigDecimal(amount);
            financeTrackerService.addManualTransaction(parsedDate, description, parsedAmount, category);
            return redirectWithMessage("Transaction added.");
        } catch (DateTimeParseException ex) {
            return redirectWithError("Invalid date format. Use YYYY-MM-DD.");
        } catch (NumberFormatException ex) {
            return redirectWithError("Invalid amount.");
        } catch (IllegalArgumentException ex) {
            return redirectWithError(ex.getMessage());
        }
    }

    // Updates an existing transaction and persists the changes to the database.
    @PostMapping("/transactions/edit")
    public String editTransaction(@RequestParam("id") Long id,
                                  @RequestParam("date") String date,
                                  @RequestParam("description") String description,
                                  @RequestParam("amount") String amount,
                                  @RequestParam(value = "category", required = false) String category) {
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            BigDecimal parsedAmount = new BigDecimal(amount);
            financeTrackerService.updateTransaction(id, parsedDate, description, parsedAmount, category);
            return redirectWithMessage("Transaction updated.");
        } catch (DateTimeParseException ex) {
            return redirectWithError("Invalid date format.");
        } catch (NumberFormatException ex) {
            return redirectWithError("Invalid amount.");
        } catch (IllegalArgumentException ex) {
            return redirectWithError(ex.getMessage());
        }
    }

    // Deletes a transaction by its ID with confirmation message.
    @PostMapping("/transactions/delete")
    public String deleteTransaction(@RequestParam("id") Long id) {
        try {
            financeTrackerService.deleteTransaction(id);
            return redirectWithMessage("Transaction deleted.");
        } catch (IllegalArgumentException ex) {
            return redirectWithError(ex.getMessage());
        }
    }

    // Processes multipart file CSV upload and imports transactions in batch.
    @PostMapping("/transactions/upload")
    public String uploadTransactions(@RequestParam("file") MultipartFile file) {
        try {
            int imported = financeTrackerService.importFromCsv(file);
            return redirectWithMessage("Imported " + imported + " transaction(s).");
        } catch (IllegalArgumentException ex) {
            return redirectWithError(ex.getMessage());
        } catch (IOException ex) {
            return redirectWithError("Unable to read uploaded file.");
        }
    }

    // Loads predefined sample transaction data for demonstration purposes.
    @PostMapping("/transactions/load-sample")
    public String loadSampleData() {
        try {
            int imported = financeTrackerService.importSampleData();
            return redirectWithMessage("Loaded " + imported + " sample transaction(s).");
        } catch (IOException ex) {
            return redirectWithError("Unable to load sample transactions.");
        }
    }

    // Clears all transactions from the database.
    @PostMapping("/transactions/clear")
    public String clearTransactions() {
        int clearedCount = financeTrackerService.clearTransactions();
        return redirectWithMessage("Cleared " + clearedCount + " transaction(s).");
    }

    // Fallback handler for GET request on clear endpoint with error message.
    @GetMapping("/transactions/clear")
    public String clearTransactionsGetFallback() {
        return redirectWithError("Use the Clear Transaction Data button to submit this action.");
    }

    // Undoes the last finance operation and restores previous state.
    @PostMapping("/transactions/undo")
    public String undoLastAction() {
        String result = financeTrackerService.undoLastAction();
        if (result != null) {
            return redirectWithMessage(result);
        }
        return redirectWithError("Nothing to undo.");
    }

    // Creates a categorization rule for auto-categorizing transactions based on keywords.
    @PostMapping("/rules")
    public String addRule(@RequestParam("pattern") String pattern,
                          @RequestParam("category") String category,
                          @RequestParam(value = "patternType", defaultValue = "KEYWORD") String patternType,
                          @RequestParam(value = "priority", defaultValue = "10") int priority) {
        try {
            financeTrackerService.addRule(patternType, pattern, category, priority);
            return redirectWithMessage("Rule added.");
        } catch (IllegalArgumentException ex) {
            return redirectWithError(ex.getMessage());
        }
    }

    // Exports the current transaction list as a CSV file for download.
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to) {
        LocalDate fromDate = parseOptionalDate(from);
        LocalDate toDate = parseOptionalDate(to);
        String csv = financeTrackerService.exportToCsv(fromDate, toDate);
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=kanso-export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvBytes.length)
                .body(csvBytes);
    }

    // Creates a monthly spending budget for a specific category with alert threshold.
    @PostMapping("/budgets")
    public String addBudget(@RequestParam("category") String category,
                           @RequestParam("monthlyLimit") String monthlyLimit,
                           @RequestParam(value = "alertThreshold", defaultValue = "80") String alertThreshold) {
        try {
            BigDecimal limit = new BigDecimal(monthlyLimit);
            BigDecimal threshold = new BigDecimal(alertThreshold);
            budgetService.createBudget(category, limit, threshold);
            return redirectWithMessage("Budget added for " + category + ".");
        } catch (NumberFormatException ex) {
            return redirectWithError("Invalid budget amount.");
        } catch (Exception ex) {
            return redirectWithError(ex.getMessage());
        }
    }

    // Removes a budget definition by its ID.
    @PostMapping("/budgets/delete")
    public String deleteBudget(@RequestParam("id") Long id) {
        try {
            budgetService.deleteBudget(id);
            return redirectWithMessage("Budget removed.");
        } catch (Exception ex) {
            return redirectWithError(ex.getMessage());
        }
    }

    // Populates the model with dashboard data including transactions, trends, budgets, and analytics.
    private void populateModel(Model model, LocalDate from, LocalDate to) {
        boolean filtered = from != null || to != null;
        List<TransactionDto> transactions = filtered
                ? financeTrackerService.getTransactions(from, to)
                : financeTrackerService.getTransactions();

        BigDecimal netFlow = financeTrackerService.getNetFlow();
        model.addAttribute("transactionCount", financeTrackerService.getTransactionCount());
        model.addAttribute("totalIncome", financeTrackerService.getTotalIncome());
        model.addAttribute("totalExpense", financeTrackerService.getTotalExpense());
        model.addAttribute("netFlow", netFlow);
        model.addAttribute("netPositive", netFlow.signum() >= 0);
        model.addAttribute("transactions", transactions);
        model.addAttribute("monthlyTrends", financeTrackerService.getMonthlyTrends());
        model.addAttribute("unusualTransactions", financeTrackerService.detectUnusualTransactions());
        model.addAttribute("categorySpend", financeTrackerService.getCurrentMonthCategorySpend());
        model.addAttribute("customRules", financeTrackerService.getCustomRules());
        model.addAttribute("defaultRules", financeTrackerService.getDefaultRules());

        // B5: Top 3 spending categories
        model.addAttribute("topCategories", financeTrackerService.getTopCategories(3));
        // B7: Savings rate
        model.addAttribute("savingsRate", financeTrackerService.getSavingsRatePercent());
        model.addAttribute("savingsRateLevel", financeTrackerService.getSavingsRateLevel());
        // C9: Recurring transactions
        model.addAttribute("recurringTransactions", financeTrackerService.detectRecurringTransactions());
        // D18: Rule suggestions
        model.addAttribute("ruleSuggestions", financeTrackerService.suggestRules());

        // Budget statuses (real-time spend vs limit)
        model.addAttribute("budgets", budgetService.getAllBudgets());
        model.addAttribute("budgetStatuses", budgetService.getBudgetStatuses());

        // Recent activity (audit log)
        model.addAttribute("recentActivity", auditService.getRecentActivity(10));
    }

    // Safely parses an optional date string with null-safe handling.
    private LocalDate parseOptionalDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // Builds a redirect URL with a success message parameter.
    private String redirectWithMessage(String message) {
        return "redirect:/?message=" + urlEncode(message);
    }

    // Builds a redirect URL with an error message parameter.
    private String redirectWithError(String message) {
        return "redirect:/?error=" + urlEncode(message);
    }

    // URL-encodes a string for safe use in redirect parameters.
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
