package org.esg;

import com.opencsv.CSVReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

//Klasse startet Spring Boot aber beendet sich nach dem Report
@SpringBootApplication
public class TerminalEnergyReport {

    public static void main(String[] args) {
        //Startet Spring Boot, führt CommandLineRunner aus, dann exit
        SpringApplication app = new SpringApplication(TerminalEnergyReport.class);
        app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE); //momentan noch kein web server
        app.run(args).close(); //Beendet nach Ausführung
    }

    @Bean
    public CommandLineRunner runReport() {
        return args -> {
            System.out.println("=== Lese CSV und generiere VSME-Energiebericht ===");

            var reader = new CSVReader(new InputStreamReader(
                    TerminalEnergyReport.class.getResourceAsStream("/data/Innolab.csv"),
                    StandardCharsets.UTF_8
            ));

            Map<String, BigDecimal> consumption = new LinkedHashMap<>();
            String currentPeriod = null;

            for (String[] row : reader.readAll()) {
                if (row.length == 0 || row[0] == null) continue;

                String firstCell = row[0].trim();

                //Neue Periode erkannt (z.B. ";2024/2025 Bauteil B;;...")
                if (firstCell.startsWith(";") && firstCell.contains("Bauteil")) {
                    currentPeriod = firstCell.replace(";", "").trim();
                    continue;
                }

                //Datenzeile (z.B. "Sep.24;18990;kWh;...")
                if (!firstCell.isEmpty() && !firstCell.chars().allMatch(c -> c == ';')) {
                    if (row.length >= 9) {
                        addValue(consumption, "Bauteil B", row[1]);
                        addValue(consumption, "Bauteil C", row[3]);
                        addValue(consumption, "Bauteil A & F", row[5]);
                    }
                }
            }

            BigDecimal total = consumption.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String vsmeText = String.format(
                    "Der Gesamtenergieverbrauch der FHTW betrug %.0f kWh. Die erfassten Bauteile verbrauchten: %s.",
                    total,
                    consumption.entrySet().stream()
                            .map(e -> e.getKey() + ": " + e.getValue().setScale(0, BigDecimal.ROUND_HALF_UP) + " kWh")
                            .reduce((a, b) -> a + "; " + b)
                            .orElse("")
            );

            System.out.println("\n✅ VSME ENERGY REPORT:");
            System.out.println(vsmeText);
            System.out.println("\nDetails:");
            consumption.forEach((dept, value) ->
                    System.out.println("  " + dept + ": " + value + " kWh"));
            System.out.println("  Gesamt: " + total + " kWh");
        };
    }

    private static void addValue(Map<String, BigDecimal> map, String key, String valueStr) {
        if (valueStr == null || valueStr.isBlank() || "xx".equalsIgnoreCase(valueStr)) return;
        try {
            String clean = valueStr.replace(".", "").replace(",", ".");
            BigDecimal value = new BigDecimal(clean);
            map.merge(key, value, BigDecimal::add);
        } catch (NumberFormatException ignored) {}
    }
}