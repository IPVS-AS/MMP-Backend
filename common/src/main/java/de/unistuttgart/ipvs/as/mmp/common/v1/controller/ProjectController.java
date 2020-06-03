package de.unistuttgart.ipvs.as.mmp.common.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.Project;
import de.unistuttgart.ipvs.as.mmp.common.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static de.unistuttgart.ipvs.as.mmp.common.v1.controller.ProjectController.PATH;

@Controller
@CrossOrigin
@RequestMapping(value = PATH)
public class ProjectController {

    private final ProjectService projectService;
    public static final String PATH = "/v1/projects";
    private static final String PROJECT_ID_PATTERN = "/{projectId}";

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<URI> saveProject(@RequestBody Project project) {
        Project savedProject = projectService.saveProject(project);
        return ResponseEntity.created(URI.create(String.format("%s/%s", PATH, savedProject.getId()))).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllProjects() {
        projectService.deleteAllProjects();
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = PROJECT_ID_PATTERN)
    public ResponseEntity<Project> getProjectById(@PathVariable Long projectId) {
        Optional<Project> projectOptional = projectService.getProjectById(projectId);
        return projectOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = PROJECT_ID_PATTERN)
    public ResponseEntity<Project> updateProjectById(@PathVariable Long projectId, @RequestBody Project project) {
        Project updatedProject = projectService.updateProjectById(projectId, project);
        if (updatedProject == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping(value = PROJECT_ID_PATTERN)
    public ResponseEntity<Void> deleteProjectById(@PathVariable Long projectId) {
        if (projectService.deleteProjectById(projectId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
