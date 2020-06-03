package de.unistuttgart.ipvs.as.mmp.model.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import de.unistuttgart.ipvs.as.mmp.common.exception.MMPExceptionHandler;
import de.unistuttgart.ipvs.as.mmp.common.util.DefaultDataBuilder;
import de.unistuttgart.ipvs.as.mmp.model.service.OpcuaInformationModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@WebMvcTest(controllers = {OpcuaInformationModelController.class})
public class OpcuaInformationModelControllerTest extends ControllerTest {

    private static final Long EXISITNG_ID = 123L;
    private static final Long EXISITNG_MODEL_ID = 1234L;
    private static final Long EXISITNG_PROJECT_ID = 12345L;
    private static final String ALL_MODELS_PATH = "/v1/projects/" + EXISITNG_PROJECT_ID + "/models";
    private static final String OPCUA_INFORMATION_MODELS_PATH = ALL_MODELS_PATH + "/" + EXISITNG_MODEL_ID + "/opcuainformationmodel";
    private static final String PARSE_MODEL_FILE_PATH = ALL_MODELS_PATH + "/opcuainformationmodel" + "/parse";
    private static final String MODEL_FILE_ID_PATH = OPCUA_INFORMATION_MODELS_PATH + "/" + EXISITNG_ID;
    private static final String MODEL_FILE_RAW_PATTER = MODEL_FILE_ID_PATH + "/raw";

    MockMvc rest;
    @MockBean
    private OpcuaInformationModelService opcuaInformationModelService;

    @Autowired
    private OpcuaInformationModelController opcuaInformationModelController;

    private ModelMetadata testModelMetadata = DefaultDataBuilder.getNewMetadataBuilder().build();
    private Model testModel = Model.builder().modelMetadata(testModelMetadata).build();
    private MultipartFile testMultipartFile = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());
    private DBFile testDbFile = DBFile.builder().fileName("filename.txt").fileType("text/plain").build();
    private OpcuaInformationModel testOpcuaInformationModel
            = OpcuaInformationModel.builder().dbFile(testDbFile).build();
    private OpcuaInformationModel testOpcuaInformationModel2
            = OpcuaInformationModel.builder().dbFile(testDbFile).build();
    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) throws IOException {
        testOpcuaInformationModel.setModel(testModel);
        this.rest = MockMvcBuilders.standaloneSetup(this.opcuaInformationModelController)
                .setControllerAdvice(new MMPExceptionHandler(), this.opcuaInformationModelService)
                .apply(documentationConfiguration(restDocumentation).
                        operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
        testDbFile.setData(testMultipartFile.getBytes());
    }

    @Test
    public void shouldGetOpcuaInformationModelById() throws Exception {
        given(opcuaInformationModelService.getOpcuaInformationModelById(EXISITNG_ID))
                .willReturn(Optional.of(testOpcuaInformationModel));
        rest.perform(get(MODEL_FILE_ID_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dbFile.fileName")
                        .value(testOpcuaInformationModel.getDbFile().getFileName()))
                .andDo(document("opcua-get"));
    }

    @Test
    public void shouldGetAllOpcuaInformationModels() throws Exception {
        given(opcuaInformationModelService.getAllOpcuaInformationModelsByModelAndProjectId(EXISITNG_PROJECT_ID,EXISITNG_MODEL_ID))
                .willReturn(Arrays.asList(testOpcuaInformationModel, testOpcuaInformationModel2));
        rest.perform(get(OPCUA_INFORMATION_MODELS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andDo(document("opcua-get-all"));
    }

    @Test
    public void shouldNotGetAnyOpcuaInformationModel() throws Exception {
        given(opcuaInformationModelService.getAllOpcuaInformationModelsByModelAndProjectId(EXISITNG_PROJECT_ID,EXISITNG_MODEL_ID))
                .willReturn(Collections.emptyList());
        rest.perform(get(OPCUA_INFORMATION_MODELS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void shouldGetOpcuaInformationModelRawById() throws Exception {
        given(opcuaInformationModelService.getOpcuaInformationModelRaw(EXISITNG_ID))
                .willReturn(Optional.of(testDbFile));
        rest.perform(get(MODEL_FILE_RAW_PATTER))
                .andExpect(status().isOk())
                .andExpect(content().string("some xml"))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andDo(document("opcua-get-raw"));
    }

    @Test
    public void shouldNotGetOpcuaInformationModelRawById() throws Exception {
        given(opcuaInformationModelService.getOpcuaInformationModelRaw(EXISITNG_ID))
                .willReturn(Optional.empty());
        rest.perform(get(MODEL_FILE_RAW_PATTER))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldSaveOpcuaInformationModel() throws Exception {
        testOpcuaInformationModel.setId(EXISITNG_ID);
        given(opcuaInformationModelService.saveOpcuaInformationModel(any(), any(), any(), any(), any()))
                .willReturn(testOpcuaInformationModel);
        rest.perform(MockMvcRequestBuilders.multipart(OPCUA_INFORMATION_MODELS_PATH)
                .file((MockMultipartFile) testMultipartFile))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl(OPCUA_INFORMATION_MODELS_PATH + "/" + EXISITNG_ID))
                .andDo(document("opcua-create"));
    }

    @Test
    public void shouldNotSaveOpcuaInformationModel() throws Exception {
        given(opcuaInformationModelService.saveOpcuaInformationModel(any(), any(), any(), any(), any()))
                .willReturn(null);
        rest.perform(MockMvcRequestBuilders.multipart(OPCUA_INFORMATION_MODELS_PATH)
                .file((MockMultipartFile) testMultipartFile))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldParseOpcuaInformationModel() throws Exception {
        testOpcuaInformationModel.setId(EXISITNG_ID);
        given(opcuaInformationModelService.parseOpcuaInformationModelRaw(any(), any(), any()))
                .willReturn(testOpcuaInformationModel);
        rest.perform(MockMvcRequestBuilders.multipart(PARSE_MODEL_FILE_PATH)
                .file((MockMultipartFile) testMultipartFile))
                .andExpect(status().isOk())
                .andDo(document("opcua-parse"));
    }

    @Test
    public void shouldDeleteOpcuaInformationModel() throws Exception {
        given(opcuaInformationModelService.deleteOpcuaInformationModel
                (EXISITNG_ID, EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID))
                .willReturn(true);
        rest.perform(delete(MODEL_FILE_ID_PATH))
                .andExpect(status().isNoContent())
                .andDo(document("opcua-delete"));
    }

    @Test
    public void shouldNotDeleteOpcuaInformationModel() throws Exception {
        given(opcuaInformationModelService
                .deleteOpcuaInformationModel(EXISITNG_ID, EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID))
                .willReturn(false);
        rest.perform(delete(MODEL_FILE_ID_PATH))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteAllOpcuaInformationModels() throws Exception {
        given(opcuaInformationModelService.deleteAllOpcuaInformationModelsForModel(EXISITNG_PROJECT_ID,EXISITNG_MODEL_ID))
                .willReturn(true);
        rest.perform(delete(OPCUA_INFORMATION_MODELS_PATH))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldNotDeleteOpcuaInformationModels() throws Exception {
        given(opcuaInformationModelService.deleteAllOpcuaInformationModelsForModel(EXISITNG_PROJECT_ID,EXISITNG_MODEL_ID))
                .willReturn(false);
        rest.perform(delete(OPCUA_INFORMATION_MODELS_PATH))
                .andExpect(status().isNotFound());
    }
}
