package org.esg;

import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final WaterRepo repo;
    @Value("${user.dir}")
    private String userDir;

    public DataInitializer(WaterRepo repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) throws Exception {
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        try (var reader = new CSVReader(new InputStreamReader(new java.io.FileInputStream(userDir + "/Wasserverbrauchsdaten Innolab.csv")))) {
            List<String[]> rows = reader.readAll();
            for (int i = 2; i < rows.size(); i += 3) {
                if (i + 1 >= rows.size()) break;
                String[] dateRow = rows.get(i);
                String[] valRow = rows.get(i + 1);
                processBlock(dateRow, valRow, "B", 0, 3, formatter);
                processBlock(dateRow, valRow, "A", 6, 9, formatter);
                processBlock(dateRow, valRow, "F", 12, 15, formatter);
            }
            System.out.println("Import abgeschlossen.");
        }
    }

    private void processBlock(String[] dates, String[] vals, String dept, int dCol, int vCol, DateTimeFormatter fmt) {
        if (dates.length > dCol + 1 && vals.length > vCol && !dates[dCol].isEmpty() && !vals[vCol].isEmpty()) {
            try {
                LocalDate start = LocalDate.parse(dates[dCol], fmt);
                LocalDate end = LocalDate.parse(dates[dCol + 1], fmt);
                BigDecimal cons = new BigDecimal(vals[vCol].replace(",", "."));
                WaterRecord record = new WaterRecord();
                record.setDept(dept);
                record.setStartDate(start);
                record.setEndDate(end);
                record.setConsumption(cons);
                repo.save(record);
            } catch (Exception ex) {
                System.err.println("Skippe Zeile für " + dept + ": " + ex.getMessage());
            }
        }
    }
}