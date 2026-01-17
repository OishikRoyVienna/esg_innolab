package org.esg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawEnergyImportRepository extends JpaRepository<RawEnergyImport, Long> {
}
