package de.unistuttgart.ipvs.as.mmp.eam.repository;

import de.unistuttgart.ipvs.as.mmp.eam.domain.BusinessProcess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessProcessRepository extends JpaRepository<BusinessProcess, Long> {
    Optional<BusinessProcess> findByLabel(String name);
}
