package org.esg.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.esg.models.*;
import org.esg.repositories.LocationRepository;
import org.esg.repositories.MeasurementRepository;
import org.esg.repositories.MetricRepository;
import org.esg.repositories.PeriodRepository;
import org.esg.repositories.RawEnergyImportRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnergyAggregationService {

    private final RawEnergyImportRepository rawRepo;
    private final PeriodRepository periodRepo;
    private final LocationRepository locationRepo;
    private final MetricRepository metricRepo;
    private final MeasurementRepository measurementRepo;

    @Transactional
    public void aggregate() {
        // Stelle sicher, dass die Metrik existiert
        Metric metric = metricRepo.findByName("Energy Consumption")
                .orElseGet(() -> {
                    Metric m = new Metric();
                    m.setName("Energy Consumption");
                    return metricRepo.save(m);
                });

        for (RawEnergyImport r : rawRepo.findAll()) {
            // Periode erstellen oder laden
            Period period = periodRepo.findByYearLabel(r.getYearLabel())
                    .orElseGet(() -> {
                        Period p = new Period();
                        p.setYearLabel(r.getYearLabel());
                        return periodRepo.save(p);
                    });

            // Standort (Location) erstellen oder laden
            Location location = locationRepo.findByName(r.getLocation())
                    .orElseGet(() -> {
                        Location l = new Location();
                        l.setName(r.getLocation());
                        return locationRepo.save(l);
                    });

            // Prüfen, ob Messung bereits existiert
            Optional<Measurement> existing = measurementRepo
                    .findByLocationAndMetricAndPeriodAndYearAndMonth(
                            location, metric, period,
                            r.getYear(), r.getMonth());

            Measurement measurement;
            if (existing.isPresent()) {
                measurement = existing.get(); // Update
            } else {
                measurement = new Measurement(); // Neu anlegen
                measurement.setLocation(location);
                measurement.setMetric(metric);
                measurement.setPeriod(period);
                measurement.setYear(r.getYear());
                measurement.setMonth(r.getMonth());
            }

            // Werte setzen
            measurement.setUnit(r.getUnit());
            try {
                measurement.setValue(new BigDecimal(r.getValue()));
            } catch (NumberFormatException e) {
                // Überspringe ungültige Werte (z. B. "xx")
                continue;
            }

            measurementRepo.save(measurement);
        }
    }
}