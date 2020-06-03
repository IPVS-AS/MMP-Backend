package de.unistuttgart.ipvs.as.mmp.model.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelGroup;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.Project;
import de.unistuttgart.ipvs.as.mmp.common.domain.User;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.service.ProjectService;
import de.unistuttgart.ipvs.as.mmp.common.util.DefaultDataBuilder;
import de.unistuttgart.ipvs.as.mmp.eam.service.EAMContainerService;
import de.unistuttgart.ipvs.as.mmp.model.repository.ModelRepository;
import de.unistuttgart.ipvs.as.mmp.model.service.impl.ModelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class ModelServiceTest {

    private static final String EXISITNG_PROJECT_NAME = "test project";
    private static final Long EXISITNG_ID = 123L;
    private static final Long MODEL_ID = 123L;
    private static final Long NON_EXISTING_ID = 12345L;
    private static final Long EXISTING_MODEL_GROUP_ID = 12L;

    @Autowired
    ModelService modelService;
    @Autowired
    ProjectService projectService;
    @Autowired
    ModelRepository modelRepository;
    @Autowired
    EAMContainerService eamContainerService;

    private User testUser = User.builder()
            .name("Hans Maier")
            .role("admin")
            .password("password")
            .build();
    private Project testProject = Project.builder()
            .name(EXISITNG_PROJECT_NAME)
            .build();
    private ModelMetadata testModelMetadata = DefaultDataBuilder.getNewMetadataBuilder().build();
    private Model testModel = Model.builder()
            .modelMetadata(testModelMetadata)
            .project(testProject)
            .build();

    private ModelGroup modelGroup = ModelGroup.builder().modelGroupName("test group").build();


    @BeforeEach
    public void setUp() {
        testModelMetadata.setAuthor(testUser);
        testProject.setModels(Collections.singletonList(testModel));
        testProject.setId(EXISITNG_ID);
        testModel.setId(EXISITNG_ID);
        given(projectService.getProjectById(EXISITNG_ID)).willReturn(Optional.of(testProject));
        given(projectService.getProjectById(NON_EXISTING_ID)).willReturn(Optional.empty());
        given(projectService.existsProjectById(EXISITNG_ID)).willReturn(true);
        given(modelRepository.findAllByProjectId(EXISITNG_ID)).willReturn(Collections.singletonList(testModel));
        given(modelRepository.findById(EXISITNG_ID)).willReturn(Optional.of(testModel));
        given(modelRepository.findAllModelGroupsByProjectId(EXISITNG_ID)).willReturn(Collections.singletonList(modelGroup));
        given(modelRepository.findMaxModelVersionByModelGroupId(any())).willReturn(Optional.of(1L));
        given(modelRepository.findLastModelByProjectIdAndModelGroupId(EXISITNG_ID, EXISTING_MODEL_GROUP_ID)).willReturn(Optional.of(testModel));
        given(modelRepository.findAllByProjectIdAndModelMetadata_ModelGroup_id(EXISITNG_ID, EXISTING_MODEL_GROUP_ID)).willReturn(Collections.singletonList(testModel));
    }

    @Test
    public void shouldGetAllModels() {
        assertEquals(Collections.singletonList(testModel), modelService.getAllModelsForProject(EXISITNG_ID));
    }

    @Test
    public void shouldGetModelById() {
        Optional<Model> model = modelService.getModelForProjectById(EXISITNG_ID, MODEL_ID);
        assertEquals(Optional.of(testModel), model);
    }

    @Test
    public void shouldNotGetModelById() {
        assertThrows(IdException.class,
                () -> modelService.getModelForProjectById(NON_EXISTING_ID, EXISITNG_ID)
        );
    }

    @Test
    public void shouldGetLastModel() {
        Optional<Model> model = modelService.getLastModelForProjectAndModelGroupIdentifier(EXISITNG_ID, EXISTING_MODEL_GROUP_ID);
        assertEquals(Optional.of(testModel), model);
    }

    @Test
    public void shouldGetAllModelGroupIds() {
        List<ModelGroup> modelGroups = modelService.getAllModelGroupsForProject(EXISITNG_ID);
        assertTrue(modelGroups.containsAll(Collections.singleton(modelGroup)));
    }

    @Test
    public void shouldSetVersionOnModelSeave() {
        ArgumentCaptor<Model> argument = ArgumentCaptor.forClass(Model.class);
        modelService.saveModelForProject(EXISITNG_ID, testModel);
        verify(modelRepository).save(argument.capture());
        assertEquals(Long.valueOf(2L), argument.getValue().getModelMetadata().getVersion());
    }

    @Test
    public void shouldSetModelGroupIdOnModelSeave() {
        ArgumentCaptor<Model> argument = ArgumentCaptor.forClass(Model.class);
        modelService.saveModelForProject(EXISITNG_ID, testModel);
        verify(modelRepository).save(argument.capture());
        assertNotNull(argument.getValue().getModelMetadata().getModelGroup());
    }

    @Test
    public void shouldGetModelsByProjectIdAndModelGroupIdentifier() {
        List<Model> models = modelService.getModelsForProjectAndModelGroupIdentifier(EXISITNG_ID, EXISTING_MODEL_GROUP_ID);
        assertTrue(models.containsAll(Collections.singleton(testModel)));
    }

    @Test
    public void shouldDeleteModelByProjectIdAndModelId() {
        given(eamContainerService.isModelContainedInEAMContainer(MODEL_ID)).willReturn(false);
        assertTrue(modelService.deleteModelForProjectById(EXISITNG_ID, MODEL_ID));
    }

    @Test
    public void shouldNotDeleteModelByProjectIdAndModelId() {
        given(eamContainerService.isModelContainedInEAMContainer(MODEL_ID)).willReturn(true);
        assertThrows(IllegalArgumentException.class, () -> modelService.deleteModelForProjectById(EXISITNG_ID, MODEL_ID));
    }

    @Test
    public void shouldDeleteAllModelsByProjectId() {
        given(eamContainerService.isModelContainedInEAMContainer(MODEL_ID)).willReturn(false);
        modelService.deleteAllModelsForProject(EXISITNG_ID);
    }

    @Test
    public void shouldNotDeleteAllModelsByProjectId() {
        given(eamContainerService.isModelContainedInEAMContainer(MODEL_ID)).willReturn(true);
        assertThrows(IllegalArgumentException.class, () -> modelService.deleteAllModelsForProject(EXISITNG_ID));
    }

    @Test
    public void shouldNotSaveModelWithoutGroupName() {
        testModel.getModelMetadata().setModelGroup(null);
        assertThrows(IllegalArgumentException.class, () -> modelService.saveModelForProject(EXISITNG_ID, testModel));
    }

    @Test
    public void shouldNotSaveModelWithoutMetadata() {
        testModel.setModelMetadata(null);
        assertThrows(IllegalArgumentException.class, () -> modelService.saveModelForProject(EXISITNG_ID, testModel));
    }

    @Test
    public void shouldSetModelNameAsDefaultGroupName() {
        testModel.getModelMetadata().setModelGroup(new ModelGroup());
        Model model = modelService.saveModelForProject(EXISITNG_ID, testModel);
        assertEquals(model.getModelMetadata().getModelGroup().getModelGroupName(), model.getModelMetadata().getName());
    }

    @Test
    public void shouldSetNewModelGroup() {
        testModel.getModelMetadata().setModelGroup(new ModelGroup("test"));
        Model model = modelService.saveModelForProject(EXISITNG_ID, testModel);
        assertEquals(model, testModel);
    }

    @TestConfiguration
    static class ModelServiceTestContextConfiguration {

        @MockBean
        public ModelRepository modelRepository;

        @MockBean
        public ProjectService projectService;

        @MockBean
        public EAMContainerService eamContainerService;

        @Bean
        public ModelService modelService() {
            return new ModelServiceImpl(this.projectService, this.modelRepository,
                    this.eamContainerService);
        }
    }
}
