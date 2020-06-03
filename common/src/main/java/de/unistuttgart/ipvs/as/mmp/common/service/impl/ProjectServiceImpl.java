package de.unistuttgart.ipvs.as.mmp.common.service.impl;

import de.unistuttgart.ipvs.as.mmp.common.domain.Project;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.repository.ProjectRepository;
import de.unistuttgart.ipvs.as.mmp.common.service.ProjectService;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public Optional<Project> getProjectById(@NonNull Long projectId) {
        return projectRepository.findById(projectId);
    }

    @Override
    public Optional<Project> getProjectByName(@NonNull String projectName) {
        return projectRepository.findByName(projectName);
    }

    @Override
    public Project saveProject(@NonNull Project project) {
        if (!CollectionUtils.isEmpty(project.getModels())) {
            throw new IllegalArgumentException("Models should be null or empty on save");
        }
        return projectRepository.save(project);
    }

    @Override
    public Project updateProjectById(@NonNull Long projectId, @NonNull Project project) {
        if (!CollectionUtils.isEmpty(project.getModels())) {
            throw new IllegalArgumentException("Models should be null or empty on update");
        }
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent()) {
            project.setId(optionalProject.get().getId());
        } else {
            throw IdException.idNotFound(Project.class, projectId);
        }
        return projectRepository.save(project);
    }

    @Override
    public boolean existsProjectById(@NonNull Long projectId) {
        return projectRepository.existsById(projectId);
    }

    @Override
    public boolean deleteProjectById(@NonNull Long projectId) {
        if (projectRepository.existsById(projectId)) {
            projectRepository.deleteById(projectId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void deleteAllProjects() {
        projectRepository.deleteAll();
    }
}