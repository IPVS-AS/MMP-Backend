package de.unistuttgart.ipvs.as.mmp.common.repository;

import de.unistuttgart.ipvs.as.mmp.common.configuration.MmpJpaTestConfig;
import de.unistuttgart.ipvs.as.mmp.common.domain.Project;
import de.unistuttgart.ipvs.as.mmp.common.domain.ProjectStatus;
import de.unistuttgart.ipvs.as.mmp.common.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import(MmpJpaTestConfig.class)
public class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser = new User("Hans Maier", "admin", "password");
    private Project testProject;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        projectRepository.deleteAll();
        testUser = userRepository.save(testUser);
        testProject = Project.builder()
                .name("EnPro Project")
                .description("This is a description of the EnPro Project")
                .creationDate(LocalDate.now())
                .startDate(LocalDate.of(2018, 4, 1))
                .endDate(LocalDate.of(2018, 10, 1))
                .status(ProjectStatus.IN_PROGRESS)
                .useCase("lead time improvement")
                .editors(Collections.singletonList(testUser))
                .models(Collections.emptyList())
                .build();
    }

    @Test
    public void shouldFindAllProjects() {
        projectRepository.save(testProject);

        List<Project> projects = projectRepository.findAll();
        assertEquals(Collections.singletonList(testProject), projects);
    }

    @Test
    public void shouldNotFindAny() {
        List<Project> projects = projectRepository.findAll();
        assertEquals(Collections.emptyList(), projects);
    }

    @Test
    public void shouldNotFindById() {
        Optional<Project> projectOptional = projectRepository.findById(123456789L);
        assertFalse(projectOptional.isPresent());
    }

    @Test
    public void shouldFindById() {
        Long projectId = projectRepository.save(testProject).getId();

        Optional<Project> project = projectRepository.findById(projectId);
        assertEquals(Optional.of(testProject), project);
    }

    @Test
    public void shouldFindByName() {
        String projectName = projectRepository.save(testProject).getName();
        Optional<Project> projectOptional = projectRepository.findByName(projectName);
        assertEquals(Optional.of(testProject), projectOptional);
    }

    @Test
    public void shouldNotFindByName() {
        Optional<Project> projectOptional = projectRepository.findByName("No name");
        assertFalse(projectOptional.isPresent());
    }
}
