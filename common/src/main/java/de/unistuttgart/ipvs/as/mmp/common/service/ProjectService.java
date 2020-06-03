package de.unistuttgart.ipvs.as.mmp.common.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectService {
    List<Project> getAllProjects();

    Optional<Project> getProjectById(Long id);

    Project saveProject(Project project);

    Project updateProjectById(Long id, Project project);

    Optional<Project> getProjectByName(String projectName);

    boolean existsProjectById(Long projectId);

    boolean deleteProjectById(Long projectId);

    void deleteAllProjects();
}