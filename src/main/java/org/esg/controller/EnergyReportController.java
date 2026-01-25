package org.esg.controller;

import org.esg.models.Measurement;
import org.esg.repositories.MeasurementRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class EnergyReportController {

    private final MeasurementRepository measurementRepo;

    public EnergyReportController(MeasurementRepository measurementRepo) {
        this.measurementRepo = measurementRepo;
    }

    @GetMapping("/api/esg/energy/report")
    public Map<String, Object> generateVSMEEnergyReport() {
        var measurements = measurementRepo.findAll().stream()
                .filter(m -> m.getMetric() != null &&
                        "Energy Consumption".equals(m.getMetric().getName()))
                .toList();

        //Aggregiere nach Standort ()
        var byDept = measurements.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getLocation().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Measurement::getValue, BigDecimal::add)
                ));

        BigDecimal total = byDept.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        String text = String.format(
                "Der Gesamtenergieverbrauch der FHTW betrug %.0f kWh. Die erfassten Bauteile verbrauchten: %s.",
                total,
                byDept.entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue().setScale(0, BigDecimal.ROUND_HALF_UP) + " kWh")
                        .collect(Collectors.joining("; "))
        );

        return Map.of("total_kwh", total, "by_department", byDept, "vsme_text", text);
    }
}