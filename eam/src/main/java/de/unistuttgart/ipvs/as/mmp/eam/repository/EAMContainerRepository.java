package de.unistuttgart.ipvs.as.mmp.eam.repository;

import de.unistuttgart.ipvs.as.mmp.eam.domain.EAMContainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EAMContainerRepository extends JpaRepository<EAMContainer, Long> {

    List<EAMContainer> findAllByModel_Id(Long id);
}
