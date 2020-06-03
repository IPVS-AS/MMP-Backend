package de.unistuttgart.ipvs.as.mmp.model.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.*;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.service.DBFileStorageService;
import de.unistuttgart.ipvs.as.mmp.common.util.DefaultDataBuilder;
import de.unistuttgart.ipvs.as.mmp.model.repository.ModelFileRepository;
import de.unistuttgart.ipvs.as.mmp.model.service.impl.ModelFileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@ExtendWith(SpringExtension.class)
public class ModelFileServiceTest {

    private static final Long EXISITNG_ID = 123L;
    private static final Long EXISITNG_MODEL_ID = 1234L;
    private static final Long EXISITNG_PROJECT_ID = 12345L;

    @Autowired
    ModelService modelService;
    @Autowired
    ModelFileRepository modelFileRepository;
    @Autowired
    DBFileStorageService dbFileStorageService;
    @Autowired
    ModelFileService modelFileService;

    private ModelMetadata testModelMetadata = DefaultDataBuilder.getNewMetadataBuilder().build();
    private Model testModel = Model.builder().modelMetadata(testModelMetadata).build();
    private DBFile testDbFile = DBFile.builder().fileName("testFile").fileType("testFileType").build();
    private MultipartFile testMultipartFile = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());
    private ModelFile testModelFile = ModelFile.builder().model(testModel).dbFile(testDbFile).fileSize(testMultipartFile.getSize()).build();

    @BeforeEach
    public void setUp() throws IOException {
        given(modelFileRepository.findById(EXISITNG_ID)).willReturn(Optional.of(testModelFile));
        given(modelService.getModelForProjectById(EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID)).willReturn(Optional.of(testModel));
        given(dbFileStorageService.storeFile(testMultipartFile)).willReturn(testDbFile);
        given(modelFileRepository.save(testModelFile)).willReturn(testModelFile);
        given(modelService.updateModelForProjetById(EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID, testModel)).willReturn(testModel);
        doNothing().when(modelFileRepository).deleteById(EXISITNG_ID);
        doNothing().when(dbFileStorageService).deleteFile(EXISITNG_ID);
    }

    @Test
    public void shouldGetModelFileById() {
        Optional<ModelFile> modelFile = modelFileService.getModelFileById(EXISITNG_ID);
        assertEquals(Optional.of(testModelFile), modelFile);
    }

    @Test
    public void shouldGetModelFileRawById() {
        Optional<DBFile> dbFile = modelFileService.getModelFileRaw(EXISITNG_ID);
        assertEquals(testDbFile, dbFile.get());
    }

    @Test
    public void shouldSaveModelFile() {
        ModelFile modelFile = modelFileService.saveModelFile(testMultipartFile, EXISITNG_MODEL_ID, EXISITNG_PROJECT_ID);
        assertEquals(testModelFile, modelFile);
    }

    @Test
    public void shouldNotSaveModelFileNoFile() {
        given(modelService.getModelForProjectById(EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID)).willReturn(Optional.empty());
        assertThrows(IdException.class,
                () -> modelFileService.saveModelFile(testMultipartFile, EXISITNG_MODEL_ID, EXISITNG_PROJECT_ID));
    }

    @Test
    public void shouldNotSaveModelFileFailToSaveFile() throws IOException {
        given(dbFileStorageService.storeFile(testMultipartFile)).willThrow(new IOException());
        assertThrows(IllegalArgumentException.class,
                () -> modelFileService.saveModelFile(testMultipartFile, EXISITNG_MODEL_ID, EXISITNG_PROJECT_ID));
    }

    @Test
    public void shouldParsePMMLFileAsXml() throws IOException {
        Model dummyModel = new Model();
        dummyModel.setModelMetadata(new ModelMetadata());
        MultipartFile dummyMultipartFile = new MockMultipartFile("data", "filename.txt", "text/plain",
                this.getClass().getResourceAsStream("/dummy_pmml.xml"));
        Model model = modelFileService.parsePMMLModelFileInModel(dummyMultipartFile, dummyModel);
        assertEquals(model.getModelMetadata().getName(), "RPart_Model");
    }

    @Test
    public void shouldParseRModelFile() throws IOException {
        Model model = new Model();
        model.setModelMetadata(new ModelMetadata());
        MultipartFile kmeansRDSFile = new MockMultipartFile("data", "filename.xml", "text/plain",
                this.getClass().getResourceAsStream("/kmeans.rds"));
        Model actual = modelFileService.parseRModelFileInModel(kmeansRDSFile, model);
        assertEquals(actual.getModelMetadata().getPmmlMetadata().getApplicationName(), "JPMML-R");
    }

    @Test
    public void shouldParsePickleModelFile() throws IOException {
        Model model = new Model();
        model.setModelMetadata(new ModelMetadata());
        MultipartFile pklFile = new MockMultipartFile("data", "filename.pkl","text/plain",
                this.getClass().getResourceAsStream("/dummy.pkl"));
        Model actual = modelFileService.parsePickleFileInModel(pklFile, model);
        List<InputAttribute> expectedInputAttributes = Arrays.asList(
                InputAttribute.builder().name("x1").usageType("active").dataType("double").build(),
                InputAttribute.builder().name("x2").usageType("active").dataType("double").build(),
                InputAttribute.builder().name("x3").usageType("active").dataType("double").build(),
                InputAttribute.builder().name("x4").usageType("active").dataType("double").build(),
                InputAttribute.builder().name("x5").usageType("active").dataType("double").build(),
                InputAttribute.builder().name("x6").usageType("active").dataType("double").build(),
                InputAttribute.builder().name("x7").usageType("active").dataType("double").build(),
                InputAttribute.builder().name("x8").usageType("active").dataType("double").build()
                );
        List<OutputAttribute> outputAttributes = Arrays.asList(new OutputAttribute("probability(0.0)", "probability", "continuous"), new OutputAttribute("probability(1.0)", "probability", "continuous"));
        PMMLMetadata expectedPMMLMetadata = PMMLMetadata.builder().miningFunction("classification").applicationName("JPMML-SkLearn").applicationVersion("1.5.1").pmmlVersion("4.3").inputAttributes(expectedInputAttributes).outputAttributes(outputAttributes).build();
        PMMLMetadata actualPMMLMetadata = actual.getModelMetadata().getPmmlMetadata();
        assertEquals(expectedPMMLMetadata.getPmmlVersion(), actualPMMLMetadata.getPmmlVersion());
        assertEquals(expectedPMMLMetadata.getApplicationName(), actualPMMLMetadata.getApplicationName());
        assertEquals(expectedPMMLMetadata.getApplicationVersion(), actualPMMLMetadata.getApplicationVersion());
        assertEquals(expectedPMMLMetadata.getMiningFunction(), actualPMMLMetadata.getMiningFunction());
        assertEquals(expectedPMMLMetadata.getInputAttributes(), actualPMMLMetadata.getInputAttributes());
        assertEquals(expectedPMMLMetadata.getOutputAttributes(), actualPMMLMetadata.getOutputAttributes());
    }

    @Test
    public void schouldParsePMMLfile() throws IOException {
        Model model = new Model();
        model.setModelMetadata(new ModelMetadata());
        MultipartFile kmeansPMMLFile = new MockMultipartFile("data", "filename.xml", "text/plain",
                this.getClass().getResourceAsStream("/kmeans.pmml"));
        Model actual = modelFileService.parsePMMLModelFileInModel(kmeansPMMLFile, model);
        assertEquals(actual.getModelMetadata().getPmmlMetadata().getApplicationName(), "JPMML-R");
    }

    @Test
    public void shouldNotParsePMMLFile() throws IOException {
        Model dummyModel = new Model();
        dummyModel.setModelMetadata(new ModelMetadata());
        MultipartFile dummyMultipartFile = new MockMultipartFile("data", "filename.txt", "text/plain",
                this.getClass().getResourceAsStream("fail.xml"));
        assertThrows(IllegalArgumentException.class,
                () -> modelFileService.parsePMMLModelFileInModel(dummyMultipartFile, dummyModel));
    }

    @Test
    public void shouldDeleteModelFile() {
        assertTrue(modelFileService.deleteModelFile(EXISITNG_ID, EXISITNG_MODEL_ID, EXISITNG_PROJECT_ID));
    }

    @TestConfiguration
    static class ModelFileServiceTestContextConfiguration {

        @MockBean
        public ModelFileRepository modelFileRepository;

        @MockBean
        public ModelService modelService;

        @MockBean
        public DBFileStorageService dbFileStorageService;


        @Bean
        public ModelFileService modelFileService() {
            return new ModelFileServiceImpl(this.modelFileRepository, this.dbFileStorageService, this.modelService);
        }
    }
}
