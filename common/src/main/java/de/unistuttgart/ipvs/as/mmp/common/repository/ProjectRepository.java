package de.unistuttgart.ipvs.as.mmp.common.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByName(String projectName);

}