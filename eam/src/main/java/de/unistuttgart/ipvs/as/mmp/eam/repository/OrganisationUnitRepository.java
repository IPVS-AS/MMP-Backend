package de.unistuttgart.ipvs.as.mmp.eam.repository;

import de.unistuttgart.ipvs.as.mmp.eam.domain.OrganisationUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganisationUnitRepository extends JpaRepository<OrganisationUnit, Long> {

    Optional<OrganisationUnit> findByIndex(Integer index);
}
