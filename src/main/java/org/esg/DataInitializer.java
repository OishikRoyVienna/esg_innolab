package org.esg;

import com.opencsv.CSVReader;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RawEnergyImportRepository rawRepo;
    @Value("${user.dir}")
    private String userDir;

    public DataInitializer(RawEnergyImportRepository rawRepo) {
        this.rawRepo = rawRepo;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(new FileInputStream(userDir + "/Wasserverbrauchsdaten/Innolab.csv")))) {

            // Flatten CSV rows to first column for easier filtering
            List<String> rows = reader.readAll().stream()
                    .map(r -> r[0] != null ? r[0] : "")
                    .collect(Collectors.toList());

            // Remove "empty" rows where all characters are ';'
            rows = rows.stream()
                    .filter(row -> !row.chars().allMatch(c -> c == ';'))
                    .toList();

            String currentPeriod = null;

            for (String row : rows) {
                if (row.isBlank()) continue;

                if (isPeriodRow(row)) {
                    currentPeriod = extractPeriod(row);
                } else {
                    String[] data = row.split(";");
                    parseDataRow(data, currentPeriod);
                }
            }

            System.out.println("Import abgeschlossen.");
        }
    }

    /** Check if this row is a period header like ";2024/2025 ..." */
    private boolean isPeriodRow(String row) {
        return row.startsWith(";") && row.matches(";\\d{4}/\\d{4}.*");
    }

    /** Extract period string from a header row */
    private String extractPeriod(String row) {
        // Remove leading semicolon and trailing spaces
        return row.replaceFirst("^;", "").trim().split(" ")[0];
    }

    /** Parse a normal data row and save to database */
    private void parseDataRow(String[] row, String period) {
        try {
            MonthYear my = convertMonth(row[0]);
            if (my == null) return;

            saveValue(period, my, "Bauteil B", row[1], row[2]);
            saveValue(period, my, "Bauteil C", row[3], row[4]);
            saveValue(period, my, "Bauteil A & F", row[5], row[6]);
            saveValue(period, my, "Energy Base", row[7], row[8]);

        } catch (Exception ex) {
            System.err.println("Fehler bei Zeile " + Arrays.toString(row) + ": " + ex.getMessage());
        }
    }

    private void saveValue(String period, MonthYear my, String location, String val, String unit) {

        if (val == null || val.equalsIgnoreCase("xx") || val.isBlank()) return;

        RawEnergyImport e = new RawEnergyImport();
        e.setYearLabel(period);
        e.setMonth((short) my.month);
        e.setYear((short) my.year);
        e.setLocation(location);
        e.setValue(val);
        e.setUnit(unit);

        rawRepo.save(e);
    }

    private MonthYear convertMonth(String label) {

        Map<String, Integer> months = Map.ofEntries(
                Map.entry("Sep", 9), Map.entry("Okt", 10), Map.entry("Nov", 11),
                Map.entry("Dez", 12), Map.entry("Jän", 1), Map.entry("Feb", 2),
                Map.entry("Mär", 3), Map.entry("Apr", 4), Map.entry("Mai", 5),
                Map.entry("Jun", 6), Map.entry("Jul", 7), Map.entry("Aug", 8)
        );

        try {
            String[] parts = label.trim().split("\\.");
            if (parts.length != 2) return null;

            Integer month = months.get(parts[0]);
            if (month == null) return null;

            int year = 2000 + Integer.parseInt(parts[1]);

            return new MonthYear(month, year);

        } catch (Exception e) {
            return null;
        }
    }

    private record MonthYear(int month, int year) {}
}