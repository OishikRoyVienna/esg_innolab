package org.esg;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ReportController {

    private final WaterRepo repo;

    public ReportController(WaterRepo repo) {
        this.repo = repo;
    }

    @GetMapping("/api/esg/water/report")
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
}