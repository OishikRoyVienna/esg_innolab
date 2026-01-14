package org.esg;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WaterRepo extends JpaRepository<WaterRecord, Long> {
}