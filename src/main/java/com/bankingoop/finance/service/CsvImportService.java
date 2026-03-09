package com.bankingoop.finance.service;

import com.bankingoop.finance.model.CsvTransactionRow;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CsvImportService {
    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    };

    public List<CsvTransactionRow> parse(MultipartFile file) throws IOException {
        /** Parses a CSV file from multipart upload into list of transaction rows. */
        if (file == null || file.isEmpty()) {
            return List.of();
        }
        try (InputStream inputStream = file.getInputStream()) {
            return parse(inputStream);
        }
    }

    /** Parses a CSV input stream into list of transaction rows with validation. */
    public List<CsvTransactionRow> parse(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return parse(reader);
        }
    }

    /** Core CSV parsing logic with header mapping and row validation. */
    private List<CsvTransactionRow> parse(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        if (headerLine == null || headerLine.isBlank()) {
            return List.of();
        }

        List<String> headerCells = splitCsvLine(headerLine);
        Map<String, Integer> indexes = toHeaderIndexMap(headerCells);
        int dateIdx = getRequiredIndex(indexes, "date");
        int descriptionIdx = getRequiredIndex(indexes, "description");
        int amountIdx = getRequiredIndex(indexes, "amount");
        int categoryIdx = indexes.getOrDefault("category", -1);

        List<CsvTransactionRow> rows = new ArrayList<>();
        String line;
        int lineNo = 1;
        while ((line = reader.readLine()) != null) {
            lineNo++;
            if (line.isBlank()) {
                continue;
            }
            List<String> cells = splitCsvLine(line);
            try {
                LocalDate date = parseDate(readCell(cells, dateIdx));
                String description = readCell(cells, descriptionIdx);
                BigDecimal amount = parseAmount(readCell(cells, amountIdx));
                String category = categoryIdx >= 0 ? readCell(cells, categoryIdx) : "";
                rows.add(new CsvTransactionRow(date, description, amount, category));
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Invalid CSV row at line " + lineNo + ": " + ex.getMessage(), ex);
            }
        }
        return rows;
    }

    /** Maps CSV header names to column indices for flexible column ordering. */
    private Map<String, Integer> toHeaderIndexMap(List<String> headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String normalized = headers.get(i).trim().toLowerCase(Locale.ROOT);
            map.put(normalized, i);
        }
        return map;
    }

    /** Retrieves column index for required field or throws exception if missing. */
    private int getRequiredIndex(Map<String, Integer> indexes, String field) {
        Integer index = indexes.get(field);
        if (index == null) {
            throw new IllegalArgumentException("Missing required column: " + field);
        }
        return index;
    }

    /** Safely reads and trims a cell value, returning empty string if out of bounds. */
    private String readCell(List<String> cells, int index) {
        if (index < 0 || index >= cells.size()) {
            return "";
        }
        return cells.get(index).trim();
    }

    /** Parses date from text supporting multiple formats (ISO, MM/dd/yyyy, dd-MM-yyyy). */
    private LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("date is blank");
        }
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(text.trim(), formatter);
            } catch (DateTimeParseException ignored) {
                // Keep trying all supported formats.
            }
        }
        throw new IllegalArgumentException("unsupported date format: " + text);
    }

    /** Parses amount handling currency symbols, commas, and parentheses notation. */
    private BigDecimal parseAmount(String amountText) {
        if (amountText == null || amountText.isBlank()) {
            throw new IllegalArgumentException("amount is blank");
        }

        String cleaned = amountText.trim();
        boolean wrappedInParentheses = cleaned.startsWith("(") && cleaned.endsWith(")");
        if (wrappedInParentheses) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        cleaned = cleaned.replace("$", "").replace(",", "").trim();

        try {
            BigDecimal value = new BigDecimal(cleaned);
            return wrappedInParentheses ? value.negate() : value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("invalid amount: " + amountText);
        }
    }

    /** Splits CSV line respecting quoted fields and escaped quotes within fields. */
    private List<String> splitCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        cells.add(current.toString());
        return cells;
    }
}
