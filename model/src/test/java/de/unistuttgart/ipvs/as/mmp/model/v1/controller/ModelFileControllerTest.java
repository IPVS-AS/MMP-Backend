package de.unistuttgart.ipvs.as.mmp.model.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.exception.MMPExceptionHandler;
import de.unistuttgart.ipvs.as.mmp.common.util.DefaultDataBuilder;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelFileService;
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
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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
@WebMvcTest(controllers = {ModelFileController.class})
public class ModelFileControllerTest extends ControllerTest {

    private static final Long EXISITNG_ID = 123L;
    private static final Long EXISITNG_MODEL_ID = 1234L;
    private static final Long EXISITNG_PROJECT_ID = 12345L;
    private static final String ALL_MODELS_PATH = "/v1/projects/" + EXISITNG_PROJECT_ID + "/models";
    private static final String GET_MODEL_FILE = ALL_MODELS_PATH + "/" + EXISITNG_MODEL_ID + "/file";
    private static final String PARSE_MODEL_FILE_PATH = ALL_MODELS_PATH + "/file" + "/parse";
    private static final String MODEL_FILE_ID_PATH = GET_MODEL_FILE + "/" + EXISITNG_ID;
    private static final String MODEL_FILE_RAW_PATTER = MODEL_FILE_ID_PATH + "/raw";

    MockMvc rest;
    @MockBean
    private ModelFileService modelFileService;

    @Autowired
    private ModelFileController modelFileController;

    private ModelMetadata testModelMetadata = DefaultDataBuilder.getNewMetadataBuilder().build();
    private Model testModel = Model.builder().modelMetadata(testModelMetadata).build();
    private MultipartFile testMultipartFile = new MockMultipartFile("file", "filename.xml", "text/plain", "some xml".getBytes());
    private DBFile testDbFile = DBFile.builder().fileName("filename.xml").fileType("text/plain").build();
    private ModelFile testModelFile = ModelFile.builder().model(testModel).dbFile(testDbFile).build();
    private MultipartFile testRMultipartFile = new MockMultipartFile("file", "filename.rds", "text/plain", "some r code".getBytes());
    private MultipartFile testPklMultipartFile = new MockMultipartFile("file", "filename.pkl", "text/plain", "some pkl file".getBytes());

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) throws IOException {
        this.rest = MockMvcBuilders.standaloneSetup(this.modelFileController)
                .setControllerAdvice(new MMPExceptionHandler(), this.modelFileService)
                .apply(documentationConfiguration(restDocumentation).
                        operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
        testDbFile.setData(testMultipartFile.getBytes());
    }

    @Test
    public void shouldGetModelFileById() throws Exception {
        given(modelFileService.getModelFileById(EXISITNG_ID))
                .willReturn(Optional.of(testModelFile));
        rest.perform(get(MODEL_FILE_ID_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dbFile.fileName")
                        .value(testModelFile.getDbFile().getFileName()))
                .andDo(document("model-file-get"));
    }

    @Test
    public void shouldGetModelFileRawById() throws Exception {
        given(modelFileService.getModelFileRaw(EXISITNG_ID))
                .willReturn(Optional.of(testDbFile));
        rest.perform(get(MODEL_FILE_RAW_PATTER))
                .andExpect(status().isOk())
                .andExpect(content().string("some xml"))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andDo(document("model-file-get-raw"));
    }

    @Test
    public void shouldSaveModelFile() throws Exception {
        testModelFile.setId(EXISITNG_ID);
        given(modelFileService.saveModelFile(any(), any(), any()))
                .willReturn(testModelFile);
        rest.perform(MockMvcRequestBuilders.multipart(GET_MODEL_FILE)
                .file((MockMultipartFile) testMultipartFile))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl(GET_MODEL_FILE + "/" + EXISITNG_ID))
                .andDo(document("model-file-create"));
    }

    @Test
    public void shouldParseModelFile() throws Exception {
        testModelFile.setId(EXISITNG_ID);
        byte[] json = json(testModel).getBytes(StandardCharsets.UTF_8);
        MockMultipartFile jsonModelPart = new MockMultipartFile("model", "json", "text/plain", json);
        given(modelFileService.parsePMMLModelFileInModel(any(), any()))
                .willReturn(testModel);
        rest.perform(MockMvcRequestBuilders.multipart(PARSE_MODEL_FILE_PATH)
                .file((MockMultipartFile) testMultipartFile).file(jsonModelPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelMetadata.name").value(testModel.getModelMetadata().getName()))
                .andDo(document("model-file-parse"));
    }

    @Test
    public void shouldParseRModelFile() throws Exception {
        testModelFile.setId(EXISITNG_ID);
        byte[] json = json(testModel).getBytes(StandardCharsets.UTF_8);
        MockMultipartFile jsonModelPart = new MockMultipartFile("model", "json", "text/plain", json);
        given(modelFileService.parseRModelFileInModel(any(), any()))
                .willReturn(testModel);
        rest.perform(MockMvcRequestBuilders.multipart(PARSE_MODEL_FILE_PATH)
                .file((MockMultipartFile) testRMultipartFile).file(jsonModelPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelMetadata.name").value(testModel.getModelMetadata().getName()))
                .andDo(document("model-file-parse-r"));
    }

    @Test
    public void shouldParsePickleModelFile() throws Exception {
        testModelFile.setId(EXISITNG_ID);
        byte[] json = json(testModel).getBytes(StandardCharsets.UTF_8);
        MockMultipartFile jsonModelPart = new MockMultipartFile("model", "json", "text/plain", json);
        given(modelFileService.parsePickleFileInModel(any(), any()))
                .willReturn(testModel);
        rest.perform(MockMvcRequestBuilders.multipart(PARSE_MODEL_FILE_PATH)
                .file((MockMultipartFile) testPklMultipartFile).file(jsonModelPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelMetadata.name").value(testModel.getModelMetadata().getName()))
                .andDo(document("model-file-parse-pickle"));
    }

    @Test
    public void shouldDeleteModelFile() throws Exception {
        given(modelFileService.deleteModelFile(EXISITNG_ID, EXISITNG_MODEL_ID, EXISITNG_PROJECT_ID))
                .willReturn(true);
        rest.perform(delete(MODEL_FILE_ID_PATH))
                .andExpect(status().isNoContent())
                .andDo(document("model-file-delete"));
    }

    @Test
    public void shouldNotDeleteModelFile() throws Exception {
        given(modelFileService.deleteModelFile(EXISITNG_ID, EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID))
                .willReturn(false);
        rest.perform(delete(MODEL_FILE_ID_PATH))
                .andExpect(status().isNotFound());
    }

}
