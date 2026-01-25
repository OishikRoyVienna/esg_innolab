package org.esg.services;

import com.opencsv.CSVReader;
import org.esg.repositories.RawEnergyImportRepository;
import org.esg.models.RawEnergyImport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@Service
public class CsvImportService {

    private final RawEnergyImportRepository rawRepo;

    public CsvImportService(RawEnergyImportRepository rawRepo) {
        this.rawRepo = rawRepo;
    }

    @Transactional
    public void importCsv() throws Exception {

        try (var reader = new CSVReader(new InputStreamReader(new java.io.FileInputStream("C:/Users/b/Desktop/Kurse/Semester_3/InnoLab_1/esg_innolab/Wasserverbrauchsdaten/Innolab.csv")))) {
            List<String[]> rows = reader.readAll();

            // Filter out rows that are only semicolons
            rows.stream()
                    .map(row -> row[0])
                    .filter(rowStr -> rowStr != null && !rowStr.chars().allMatch(c -> c == ';'))
                    .forEach(rowStr -> {
                        String[] data = rowStr.split(";");
                        parseMonthRow(data);
                    });
        }
    }

    private void parseMonthRow(String[] row) {
        if (row.length < 9) return;

        MonthYear my = convertMonth(row[0]);
        if (my == null) return;

        saveValue(my, "Bauteil B", row[1], row[2]);
        saveValue(my, "Bauteil C", row[3], row[4]);
        saveValue(my, "Bauteil A & F", row[5], row[6]);
        saveValue(my, "Energy Base", row[7], row[8]);
    }

    private void saveValue(MonthYear my, String location, String val, String unit) {
        if (val == null || val.equalsIgnoreCase("xx") || val.isBlank()) return;

        RawEnergyImport e = new RawEnergyImport();
        e.setYearLabel(my.yearLabel());
        e.setMonth((short) my.month);
        e.setYear((short) my.year);
        e.setLocation(location);
        e.setValue(val);
        e.setUnit(unit);

        rawRepo.save(e);
    }

    private MonthYear convertMonth(String label) {
        var months = java.util.Map.ofEntries(
                Map.entry("Sep", 9), Map.entry("Okt", 10), Map.entry("Nov", 11), Map.entry("Dez", 12),
                Map.entry("Jän", 1), Map.entry("Feb", 2), Map.entry("Mär", 3), Map.entry("Apr", 4),
                Map.entry("Mai", 5), Map.entry("Jun", 6), Map.entry("Jul", 7), Map.entry("Aug", 8)
        );


        try {
            String[] parts = label.trim().split("\\.");
            if (parts.length != 2) return null;

            Integer month = months.get(parts[0]);
            if (month == null) return null;

            int year = 2000 + Integer.parseInt(parts[1]);
            return new MonthYear(month, year, parts[1]);
        } catch (Exception e) {
            return null;
        }
    }

    private record MonthYear(int month, int year, String yearLabel) {}
}
