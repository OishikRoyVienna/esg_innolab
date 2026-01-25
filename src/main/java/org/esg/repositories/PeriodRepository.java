package org.esg.repositories;

import org.esg.models.Period;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PeriodRepository extends JpaRepository<Period,Long> {
    Optional<Period> findByYearLabel(String label);
}
