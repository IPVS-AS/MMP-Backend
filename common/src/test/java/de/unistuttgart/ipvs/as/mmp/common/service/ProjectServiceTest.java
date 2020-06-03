package de.unistuttgart.ipvs.as.mmp.common.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.Project;
import de.unistuttgart.ipvs.as.mmp.common.domain.User;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.repository.ProjectRepository;
import de.unistuttgart.ipvs.as.mmp.common.service.impl.ProjectServiceImpl;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class ProjectServiceTest {

    private static final String EXISITNG_PROJECT_NAME = "test project";
    private static final String NON_EXISTING_PROJECT_NAME = "no project";
    private static final Long EXISITNG_ID = 123L;
    private static final Long NON_EXISTING_ID = 12345L;
    private User testUser = new User("Hans Maier", "admin", "password");
    private Project testProject = Project.builder()
            .name("test project")
            .editors(Collections.singletonList(testUser))
            .build();

    @TestConfiguration
    static class ProjectServiceTestContextConfiguration {

        @MockBean
        public ProjectRepository projectRepository;

        @Bean
        public ProjectService employeeService() {
            return new ProjectServiceImpl(this.projectRepository);
        }
    }

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectRepository projectRepository;

    @BeforeEach
    public void setUp() {
        given(projectRepository.findByName(EXISITNG_PROJECT_NAME)).willReturn(Optional.of(testProject));
        given(projectRepository.findByName(NON_EXISTING_PROJECT_NAME)).willReturn(Optional.empty());
        given(projectRepository.findById(EXISITNG_ID)).willReturn(Optional.of(testProject));
        given(projectRepository.findById(NON_EXISTING_ID)).willReturn(Optional.empty());
        given(projectRepository.findAll()).willReturn(Lists.newArrayList(testProject));
    }

    @Test
    public void shouldGetAllProjects() {
        List<Project> allProjects = projectService.getAllProjects();
        assertEquals(Collections.singletonList(testProject), allProjects);
    }

    @Test
    public void shouldGetProjectById() {
        Optional<Project> projectOptional = projectService.getProjectById(EXISITNG_ID);
        assertEquals(Optional.of(testProject), projectOptional);
    }

    @Test
    public void shouldNotGetProject() {
        Optional<Project> project = projectService.getProjectByName(NON_EXISTING_PROJECT_NAME);
        assertFalse(project.isPresent());
    }

    @Test
    void shouldUpdateProjectById() {
        testProject.setId(EXISITNG_ID);
        Project updatedProject = getUpdatedProject();
        given(projectRepository.save(updatedProject)).willReturn(updatedProject);
        Project project = projectService.updateProjectById(EXISITNG_ID, updatedProject);
        assertEquals("new name", project.getName());
        assertEquals(testProject.getModels(), project.getModels());
        assertEquals(testProject.getEditors(), project.getEditors());
    }

    @Test
    void shouldNotUpdateProjectById() {
        given(projectRepository.save(testProject)).willReturn(testProject);
        assertThrows(NullPointerException.class, () -> projectService.updateProjectById(EXISITNG_ID, null));
    }

    @Test
    void shouldNotUpdateProjectByIdWhenProjectNotFound() {
        assertThrows(IdException.class, () -> projectService.updateProjectById(NON_EXISTING_ID, testProject));
    }

    @Test
    void shouldGetProjectByName() {
        Optional<Project> projectOptional = projectService.getProjectByName(EXISITNG_PROJECT_NAME);
        assertEquals(Optional.of(testProject), projectOptional);
    }

    @Test
    void shouldNotGetProjectByName() {
        Optional<Project> projectOptional = projectService.getProjectByName(NON_EXISTING_PROJECT_NAME);
        assertEquals(Optional.empty(), projectOptional);
    }

    @Test
    void shouldNotSaveCauseNotEmptyModels() {
        Project project = new Project();
        project.setModels(Collections.singletonList(new Model()));
        assertThrows(IllegalArgumentException.class, () -> projectService.saveProject(project));
    }

    @Test
    void shouldNotUpdateCauseNotEmptyModels() {
        Project project = new Project();
        project.setModels(Collections.singletonList(new Model()));
        assertThrows(IllegalArgumentException.class, () -> projectService.updateProjectById(EXISITNG_ID, project));
    }

    private Project getUpdatedProject() {
        Project p = new Project();
        p.setName("new name");
        p.setModels(testProject.getModels());
        p.setEditors(testProject.getEditors());
        return p;
    }
}