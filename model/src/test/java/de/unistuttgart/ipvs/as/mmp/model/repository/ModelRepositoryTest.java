package de.unistuttgart.ipvs.as.mmp.model.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelGroup;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.Project;
import de.unistuttgart.ipvs.as.mmp.common.domain.User;
import de.unistuttgart.ipvs.as.mmp.common.util.DefaultDataBuilder;
import de.unistuttgart.ipvs.as.mmp.model.configuration.MmpJpaTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EntityScan(basePackages = "de.unistuttgart.ipvs.as.mmp.common.domain")
@EnableJpaRepositories(basePackages = "de.unistuttgart.ipvs.as.mmp.model.repository")
@Import(MmpJpaTestConfig.class)
public class ModelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ModelRepository modelRepository;

    private User testUser = new User("Hans Maier", "admin", "password");
    private Project testProject = Project.builder()
            .name("test project")
            .editors(Collections.singletonList(testUser))
            .build();
    private ModelGroup secondModelGroup = ModelGroup.builder().modelGroupName("test group 2").build();
    private ModelGroup modelGroup = ModelGroup.builder().modelGroupName("test group").build();
    private ModelMetadata testModelMetadata = DefaultDataBuilder.getNewMetadataBuilder().author(testUser).modelGroup(modelGroup).build();

    private Model testModel = Model.builder().modelMetadata(testModelMetadata).project(testProject).build();

    @BeforeEach
    public void setUp() {
        modelRepository.deleteAll();
        entityManager.persist(testUser);
        entityManager.persist(testProject);
    }

    @Test
    public void shouldFindAll() {
        entityManager.persist(testModel);

        List<Model> models = modelRepository.findAll();
        assertEquals(Collections.singletonList(testModel), models);
    }

    @Test
    public void shouldNotFindAny() {
        List<Model> models = modelRepository.findAll();
        assertEquals(Collections.emptyList(), models);
    }

    @Test
    public void shouldNotFindById() {
        Optional<Model> model = modelRepository.findById(123456789L);
        assertEquals(Optional.empty(), model);
    }

    @Test
    public void shouldFindById() {
        Long modelId = entityManager.persistAndGetId(testModel, Long.class);

        Optional<Model> model = modelRepository.findById(modelId);
        assertEquals(Optional.of(testModel), model);
    }

    @Test
    public void shouldGetAllModelGroupIdentifiers() {
        testModel.getModelMetadata().setModelGroup(modelGroup);
        ModelMetadata modelMetadata = DefaultDataBuilder.getNewMetadataBuilder().modelGroup(secondModelGroup).build();
        Model model = Model.builder().modelMetadata(modelMetadata).project(testProject).build();
        entityManager.persist(testModel);
        entityManager.persist(model);

        List<ModelGroup> modelGroups = modelRepository.findAllModelGroupsByProjectId(testProject.getId());
        assertTrue(modelGroups.containsAll(Arrays.asList(modelGroup, secondModelGroup)));
    }

    @Test
    public void shouldGetAllModelGroupIdentifiersDifferentProjcets() {
        testModel.getModelMetadata().setModelGroup(modelGroup);
        Project project = Project.builder()
                .name("test project")
                .editors(Collections.singletonList(testUser))
                .build();
        ModelMetadata modelMetadata = DefaultDataBuilder.getNewMetadataBuilder().modelGroup(secondModelGroup).build();
        Model model = Model.builder().project(project).modelMetadata(modelMetadata).build();
        entityManager.persist(project);
        entityManager.persist(testModel);
        entityManager.persist(model);

        List<ModelGroup> modelGroups = modelRepository.findAllModelGroupsByProjectId(testProject.getId());
        assertTrue(modelGroups.containsAll(Arrays.asList(modelGroup)));
    }

    @Test
    public void shouldFindMaxVersionBasedOnVersionAndModelGroupIdentifier() {
        testModel.getModelMetadata().setModelGroup(modelGroup);
        testModel.getModelMetadata().setVersion(1L);
        entityManager.persist(testModel);
        Long modelGroupId = testModel.getModelMetadata().getModelGroup().getId();
        Optional<Long> version = modelRepository.findMaxModelVersionByModelGroupId(modelGroupId);
        assertTrue(version.isPresent());
        assertEquals(version, Optional.of(1L));
    }

    @Test
    public void shouldFindMaxVersionBasedOnVersionAndModelGroupIdentifierTwoModels() {
        testModel.getModelMetadata().setModelGroup(modelGroup);
        testModel.getModelMetadata().setVersion(1L);
        ModelMetadata modelMetadata = DefaultDataBuilder.getNewMetadataBuilder().modelGroup(modelGroup).version(2L).build();
        Model model = Model.builder().project(testProject).modelMetadata(modelMetadata).build();
        entityManager.persist(testModel);
        entityManager.persist(model);
        Long modelGroupId = testModel.getModelMetadata().getModelGroup().getId();

        Optional<Long> version = modelRepository.findMaxModelVersionByModelGroupId(modelGroupId);
        assertTrue(version.isPresent());
        assertEquals(version, Optional.of(2L));
    }

    @Test
    public void shouldFindLastModelByProjectIdAndModelGroupIdentifier() {
        testModel.getModelMetadata().setModelGroup(modelGroup);
        testModel.getModelMetadata().setVersion(1L);
        ModelMetadata modelMetadata = DefaultDataBuilder.getNewMetadataBuilder().modelGroup(modelGroup).version(2L).build();
        Model model = Model.builder().project(testProject).modelMetadata(modelMetadata).build();
        entityManager.persist(testModel);
        entityManager.persist(model);
        Long modelGroupId = testModel.getModelMetadata().getModelGroup().getId();

        Optional<Model> modelOptional = modelRepository.findLastModelByProjectIdAndModelGroupId(testProject.getId(), modelGroupId);
        assertTrue(modelOptional.isPresent());
        assertEquals(modelOptional, Optional.of(model));
    }

    @Test
    public void shouldFindModelsByProjectIdAndModelGroupId() {
        testModel.getModelMetadata().setModelGroup(modelGroup);
        ModelMetadata modelMetadata = DefaultDataBuilder.getNewMetadataBuilder().modelGroup(modelGroup).version(2L).build();
        Model model = Model.builder().project(testProject).modelMetadata(modelMetadata).build();
        entityManager.persist(testModel);
        entityManager.persist(model);
        Long modelGroupId = testModel.getModelMetadata().getModelGroup().getId();

        List<Model> models = modelRepository.findAllByProjectIdAndModelMetadata_ModelGroup_id(testProject.getId(), modelGroupId);
        assertEquals(2, models.size());
        assertTrue(models.containsAll(Arrays.asList(model, testModel)));
    }

    @Test
    public void shouldFindModelsByProjectIdAndModelGroupIdDifferentProjects() {
        testModel.getModelMetadata().setModelGroup(modelGroup);
        Project project = Project.builder()
                .name("test project")
                .editors(Collections.singletonList(testUser))
                .build();
        ModelMetadata modelMetadata = DefaultDataBuilder.getNewMetadataBuilder().modelGroup(modelGroup).version(2L).build();
        Model model = Model.builder().project(project).modelMetadata(modelMetadata).build();
        entityManager.persist(project);
        entityManager.persist(testModel);
        entityManager.persist(model);
        Long modelGroupId = testModel.getModelMetadata().getModelGroup().getId();

        List<Model> models = modelRepository.findAllByProjectIdAndModelMetadata_ModelGroup_id(testProject.getId(), modelGroupId);
        assertEquals(1, models.size());
        assertTrue(models.containsAll(Arrays.asList(testModel)));
    }

    @Test
    public void shouldFindModelsByProjectIdAndModelGroupIdDifferentModelGroupIds() {
        testModel.getModelMetadata().setModelGroup(modelGroup);
        ModelMetadata modelMetadata = DefaultDataBuilder.getNewMetadataBuilder().modelGroup(secondModelGroup).version(2L).build();
        Model model = Model.builder().project(testProject).modelMetadata(modelMetadata).build();
        entityManager.persist(testModel);
        entityManager.persist(model);
        Long modelGroupId = testModel.getModelMetadata().getModelGroup().getId();

        List<Model> models = modelRepository.findAllByProjectIdAndModelMetadata_ModelGroup_id(testProject.getId(), modelGroupId);
        assertEquals(1, models.size());
        assertTrue(models.containsAll(Arrays.asList(testModel)));
    }
}
