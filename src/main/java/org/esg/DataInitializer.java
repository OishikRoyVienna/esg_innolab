package org.esg;

import com.opencsv.CSVReader;
import org.esg.models.RawEnergyImport;
import org.esg.repositories.RawEnergyImportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

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

            List<String> rows = reader.readAll().stream()
                    .map(row -> row[0])
                    .filter(row -> row != null && !row.chars().allMatch(c -> c == ';')) // remove empty/';' rows
                    .toList();

            String currentPeriod = null;

            for (String row : rows) {
                if (row.startsWith(";")) {
                    // This row defines a new period
                    currentPeriod = row.replace(";", "").trim();
                } else {
                    // Data row
                    String[] data = row.split(";");
                    parseDataRow(data, currentPeriod);
                }
            }

            System.out.println("CSV import completed.");
        }
    }

    private void parseDataRow(String[] data, String periodLabel) {
        if (data.length < 9) return; // safety

        MonthYear my = convertMonth(data[0]);
        if (my == null) return;

        saveValue(periodLabel, my, "Bauteil B", data[1], data[2]);
        saveValue(periodLabel, my, "Bauteil C", data[3], data[4]);
        saveValue(periodLabel, my, "Bauteil A & F", data[5], data[6]);
        saveValue(periodLabel, my, "Energy Base", data[7], data[8]);
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

        rawRepo.save(e); // now works because entity has an id
        rawRepo.flush();
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
