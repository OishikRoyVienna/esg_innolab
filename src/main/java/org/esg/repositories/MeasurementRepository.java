package org.esg.repositories;

import org.esg.models.Location;
import org.esg.models.Measurement;
import org.esg.models.Metric;
import org.esg.models.Period;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeasurementRepository extends JpaRepository<Measurement,Long> {
    Optional<Measurement> findByLocationAndMetricAndPeriodAndYearAndMonth(
            Location location, Metric metric, Period period, short year, short month);

}
