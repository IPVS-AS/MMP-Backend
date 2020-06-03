package de.unistuttgart.ipvs.as.mmp.model.v1.controller;


import de.unistuttgart.ipvs.as.mmp.common.domain.*;
import de.unistuttgart.ipvs.as.mmp.common.exception.MMPExceptionHandler;
import de.unistuttgart.ipvs.as.mmp.common.util.DefaultDataBuilder;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@WebMvcTest(controllers = {ModelController.class})
@AutoConfigureRestDocs
public class ModelControllerTest extends ControllerTest {

    private static final Long PROJECT_ID = 1337L;
    private static final Long MODEL_ID = 1337L;
    private static final String ALL_MODELS_PATH = ModelController.PATH + "/models";
    private static final String ALL_MODELS_PATH_BY_PROJECT = ModelController.PATH + "/projects/" + PROJECT_ID + "/models";
    private static final String MODEL_ID_PATH = ALL_MODELS_PATH_BY_PROJECT + "/" + MODEL_ID;
    private static final Long EXISTING_MODEL_GROUP_ID = 123L;
    private static final String ALL_MODELS_GROUP_IDS = ALL_MODELS_PATH_BY_PROJECT + "/modelGroups";
    private static final String MODELS_BY_GROUP_IDS = ALL_MODELS_PATH_BY_PROJECT + "/modelGroups/" + EXISTING_MODEL_GROUP_ID;
    private static final String LATEST = "/latest";

    MockMvc rest;

    @MockBean
    private ModelService modelService;

    @Autowired
    private ModelController modelController;

    private User testUser = new User("Hans Maier", "admin", "password");
    private Project testProject = Project.builder()
            .name("test project")
            .editors(Collections.singletonList(testUser))
            .build();
    private ModelMetadata testModelMetadata = DefaultDataBuilder.getNewMetadataBuilder().build();
    private ModelMetadata testSecondModelMetadata = ModelMetadata.builder()
            .name("Model2")
            .version(1L)
            .lastModified(LocalDate.now())
            .dateOfCreation(LocalDate.now())
            .algorithm("linear regression")
            .status(ModelStatus.OPERATION)
            .author(testUser)
            .build();
    private Model testModel = Model.builder().modelMetadata(testModelMetadata).project(testProject).build();
    private Model secondTestModel = Model.builder().modelMetadata(testSecondModelMetadata).project(testProject).build();
    private Model updatedTestModel = new Model();
    private ModelGroup modelGroup = ModelGroup.builder().modelGroupName("test group").build();
    private ModelGroup secondModelGroup = ModelGroup.builder().modelGroupName("test group 2").build();

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        testProject.setId(PROJECT_ID);
        this.rest = MockMvcBuilders.standaloneSetup(this.modelController)
                .setControllerAdvice(new MMPExceptionHandler(), this.modelController)
                .apply(documentationConfiguration(restDocumentation).
                        operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
        testModel.setId(MODEL_ID);
    }

    private Model getUpdatedTestModel() {
        updatedTestModel = testModel;
        updatedTestModel.getModelMetadata().setAlgorithm("test algorithm");
        updatedTestModel.getModelMetadata().setName("test algorithm");
        return updatedTestModel;
    }

    @Test
    public void shouldFindModelById() throws Exception {
        given(modelService.getModelForProjectById(PROJECT_ID, MODEL_ID))
                .willReturn(Optional.of(testModel));
        rest.perform(get(MODEL_ID_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelMetadata.name")
                        .value(testModel.getModelMetadata().getName()))
                .andDo(document("model-get"));
    }

    @Test
    public void shouldNotFindModel() throws Exception {
        given(modelService.getModelForProjectById(PROJECT_ID, MODEL_ID))
                .willReturn(Optional.empty());
        rest.perform(get(MODEL_ID_PATH))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateModelById() throws Exception {
        updatedTestModel = getUpdatedTestModel();
        given(modelService.updateModelForProjetById(any(), any(), any())).willReturn(updatedTestModel);
        rest.perform(put(MODEL_ID_PATH).contentType(MediaType.APPLICATION_JSON).content(json(updatedTestModel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelMetadata.name").value(updatedTestModel.getModelMetadata().getName()))
                .andDo(document("model-update"));
    }

    @Test
    public void shouldNotUpdateModelById() throws Exception {
        updatedTestModel = getUpdatedTestModel();
        given(modelService.updateModelForProjetById(PROJECT_ID, MODEL_ID, updatedTestModel)).willReturn(null);
        rest.perform(put(MODEL_ID_PATH).contentType(MediaType.APPLICATION_JSON).content(json(updatedTestModel)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteModelById() throws Exception {
        given(modelService.deleteModelForProjectById(PROJECT_ID, MODEL_ID)).willReturn(true);
        rest.perform(delete(MODEL_ID_PATH))
                .andExpect(status().isNoContent())
                .andDo(document("model-delete"));
    }

    @Test
    public void shouldNotDeleteModelNotFound() throws Exception {
        given(modelService.deleteModelForProjectById(PROJECT_ID, MODEL_ID)).willReturn(false);
        rest.perform(delete(MODEL_ID_PATH))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldFindAllModels() throws Exception {
        given(modelService.getAllModels()).willReturn(Arrays.asList(testModel, secondTestModel));
        rest.perform(get(ALL_MODELS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andDo(document("model-get-all"));
    }

    @Test
    public void shouldFindAllModelsByProjectId() throws Exception {
        given(modelService.getAllModelsForProject(PROJECT_ID)).willReturn(Arrays.asList(testModel, secondTestModel));
        rest.perform(get(ALL_MODELS_PATH_BY_PROJECT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void shouldNotFindAnyModel() throws Exception {
        given(modelService.getAllModelsForProject(PROJECT_ID)).willReturn(Collections.emptyList());
        rest.perform(get(ALL_MODELS_PATH_BY_PROJECT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void shouldSaveModel() throws Exception {
        testProject.setId(PROJECT_ID);
        Model modelWithId = testModel;
        modelWithId.setId(MODEL_ID);
        given(modelService.saveModelForProject(any(), any()))
                .willReturn(modelWithId);
        rest.perform(post(ALL_MODELS_PATH_BY_PROJECT).contentType(MediaType.APPLICATION_JSON).content(json(testModel)))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl(MODEL_ID_PATH))
                .andDo(document("model-create"));
    }

    @Test
    public void shouldNotSaveModel() throws Exception {
        given(modelService.saveModelForProject(any(), any()))
                .willThrow(new NullPointerException());
        rest.perform(post(ALL_MODELS_PATH_BY_PROJECT).contentType(MediaType.APPLICATION_JSON).content(json(testModel)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldDeleteAllModels() throws Exception {
        doNothing().when(modelService).deleteAllModelsForProject(PROJECT_ID);
        rest.perform(delete(ALL_MODELS_PATH_BY_PROJECT))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shoudlGetModelGroupIdentifiersByProject() throws Exception {
        given(modelService.getAllModelGroupsForProject(PROJECT_ID))
                .willReturn(Arrays.asList(modelGroup, secondModelGroup));
        rest.perform(get(ALL_MODELS_GROUP_IDS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andDo(document("model-group-get-all"));
    }

    @Test
    public void shoudlGetModelGroupIdentifiersByProjectEmptyList() throws Exception {
        given(modelService.getAllModelGroupsForProject(PROJECT_ID))
                .willReturn(Collections.emptyList());
        rest.perform(get(ALL_MODELS_GROUP_IDS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void shouldGetLastModelForProjectAndModelGroupIdentifier() throws Exception {
        given(modelService.getLastModelForProjectAndModelGroupIdentifier(PROJECT_ID, EXISTING_MODEL_GROUP_ID))
                .willReturn(Optional.of(testModel));
        rest.perform(get(MODELS_BY_GROUP_IDS + LATEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelMetadata.name")
                        .value(testModel.getModelMetadata().getName()))
                .andDo(document("model-group-get-latest"));
    }

    @Test
    public void shouldNotGetLastModelForProjectAndModelGroupIdentifier() throws Exception {
        given(modelService.getLastModelForProjectAndModelGroupIdentifier(PROJECT_ID, EXISTING_MODEL_GROUP_ID))
                .willReturn(Optional.empty());
        rest.perform(get(MODELS_BY_GROUP_IDS + LATEST))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetAllModelsForProjectAndModelGroupIdentifier() throws Exception {
        given(modelService.getModelsForProjectAndModelGroupIdentifier(PROJECT_ID, EXISTING_MODEL_GROUP_ID))
                .willReturn(Collections.singletonList(testModel));
        rest.perform(get(MODELS_BY_GROUP_IDS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andDo(document("model-group-get"));
    }

    @Test
    public void shouldGetAllModelsForProjectAndModelGroupIdentifierEmptyList() throws Exception {
        given(modelService.getModelsForProjectAndModelGroupIdentifier(PROJECT_ID, EXISTING_MODEL_GROUP_ID))
                .willReturn(Collections.emptyList());
        rest.perform(get(MODELS_BY_GROUP_IDS))
                .andExpect(status().isNotFound());
    }

}
