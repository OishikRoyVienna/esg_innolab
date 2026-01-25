package org.esg.controller;

import org.esg.WaterRepo;
import org.esg.services.CsvImportService;
import org.esg.services.EnergyAggregationService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/esg")
public class ReportController {

    private final WaterRepo repo;
    private final CsvImportService importService;
    private final EnergyAggregationService aggregationService;

    public ReportController(WaterRepo repo, CsvImportService importService, EnergyAggregationService aggregationService) {
        this.repo = repo;
        this.importService = importService;
        this.aggregationService = aggregationService;
    }

    @GetMapping("/water/report")
    public Map<String, Object> report() {
        var all = repo.findAll();
        var byDept = all.stream()
                .collect(Collectors.groupingBy(w -> w.getDept(),
                        Collectors.reducing(BigDecimal.ZERO, w -> w.getConsumption(), BigDecimal::add)));
        BigDecimal total = byDept.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        String text = String.format(
                "Der Gesamtwasserverbrauch der FHTW betrug %.0f m³. Die drei erfassten Bauteile verbrauchten: %s.",
                total,
                byDept.entrySet().stream()
                        .map(e -> "Bauteil " + e.getKey() + ": " + e.getValue().setScale(0, BigDecimal.ROUND_HALF_UP) + " m³")
                        .collect(Collectors.joining("; "))
        );
        return Map.of("total_m3", total, "by_department", byDept, "vsme_text", text);
    }

    @PostMapping("/import")
    public String importCsv() {
        try {
            importService.importCsv();
            aggregationService.aggregate();
            return "CSV imported and aggregated.";
        } catch (Exception e) {
            return "CSV import failed: " + e.getMessage();
        }
    }
}
