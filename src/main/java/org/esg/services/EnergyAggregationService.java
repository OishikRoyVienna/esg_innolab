package org.esg.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.esg.RawEnergyImportRepository;
import org.esg.models.*;
import org.esg.repositories.LocationRepository;
import org.esg.repositories.MeasurementRepository;
import org.esg.repositories.MetricRepository;
import org.esg.repositories.PeriodRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

        Metric metric = metricRepo.findByName("Energy Consumption")
                .orElseGet(() -> metricRepo.save(new Metric(null,"Energy Consumption")));

        for (RawEnergyImport r : rawRepo.findAll()) {

            Period p = periodRepo.findByYearLabel(r.getYearLabel())
                    .orElseGet(() -> periodRepo.save(new Period(null,r.getYearLabel())));

            Location loc = locationRepo.findByName(r.getLocation())
                    .orElseGet(() -> locationRepo.save(new Location(null,r.getLocation())));

            var existing = measurementRepo
                    .findByLocationAndMetricAndPeriodAndYearAndMonth(
                            loc, metric, p,
                            r.getYear(), r.getMonth());

            Measurement m;

            if (existing.isPresent()) {
                m = existing.get();          // UPDATE existing row
            } else {
                m = new Measurement();       // CREATE new row
                m.setLocation(loc);
                m.setMetric(metric);
                m.setPeriod(p);
                m.setYear(r.getYear());
                m.setMonth(r.getMonth());
            }

            m.setUnit(r.getUnit());
            m.setValue(new BigDecimal(r.getValue()));

            measurementRepo.save(m);

        }
    }
}
