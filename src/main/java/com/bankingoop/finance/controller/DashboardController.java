package com.bankingoop.finance.controller;

import com.bankingoop.finance.service.FinanceTrackerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Controller
public class DashboardController {
    private final FinanceTrackerService financeTrackerService;

    public DashboardController(FinanceTrackerService financeTrackerService) {
        this.financeTrackerService = financeTrackerService;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(value = "message", required = false) String message,
                            @RequestParam(value = "error", required = false) String error,
                            Model model) {
        populateModel(model);
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        model.addAttribute("today", LocalDate.now());
        return "dashboard";
    }

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

    @PostMapping("/transactions/load-sample")
    public String loadSampleData() {
        try {
            int imported = financeTrackerService.importSampleData();
            return redirectWithMessage("Loaded " + imported + " sample transaction(s).");
        } catch (IOException ex) {
            return redirectWithError("Unable to load sample transactions.");
        }
    }

    @PostMapping("/rules")
    public String addRule(@RequestParam("keyword") String keyword,
                          @RequestParam("category") String category) {
        try {
            financeTrackerService.addRule(keyword, category);
            return redirectWithMessage("Rule added.");
        } catch (IllegalArgumentException ex) {
            return redirectWithError(ex.getMessage());
        }
    }

    private void populateModel(Model model) {
        BigDecimal netFlow = financeTrackerService.getNetFlow();
        model.addAttribute("transactionCount", financeTrackerService.getTransactionCount());
        model.addAttribute("totalIncome", financeTrackerService.getTotalIncome());
        model.addAttribute("totalExpense", financeTrackerService.getTotalExpense());
        model.addAttribute("netFlow", netFlow);
        model.addAttribute("netPositive", netFlow.signum() >= 0);
        model.addAttribute("transactions", financeTrackerService.getTransactions());
        model.addAttribute("monthlyTrends", financeTrackerService.getMonthlyTrends());
        model.addAttribute("unusualTransactions", financeTrackerService.detectUnusualTransactions());
        model.addAttribute("categorySpend", financeTrackerService.getCurrentMonthCategorySpend());
        model.addAttribute("customRules", financeTrackerService.getCustomRules());
        model.addAttribute("defaultRules", financeTrackerService.getDefaultRules());
    }

    private String redirectWithMessage(String message) {
        return "redirect:/?message=" + urlEncode(message);
    }

    private String redirectWithError(String message) {
        return "redirect:/?error=" + urlEncode(message);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
