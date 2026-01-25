package org.esg;

import org.esg.models.WaterRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaterRepo extends JpaRepository<WaterRecord, Long> {
}