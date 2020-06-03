package de.unistuttgart.ipvs.as.mmp.model.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.service.DBFileStorageService;
import de.unistuttgart.ipvs.as.mmp.common.util.DefaultDataBuilder;
import de.unistuttgart.ipvs.as.mmp.model.repository.OpcuaInformationModelRepository;
import de.unistuttgart.ipvs.as.mmp.model.service.impl.OpcuaInformationModelServiceImpl;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@ExtendWith(SpringExtension.class)
public class OpcuainformationModelServiceTest {

    private static final Long EXISITNG_ID = 123L;
    private static final Long SECOND_EXISITNG_ID = 12L;
    private static final Long EXISITNG_MODEL_ID = 1234L;
    private static final Long NON_EXISITNG_MODEL_ID = 122312334L;
    private static final Long EXISITNG_PROJECT_ID = 12345L;
    private static final Long NON_EXISTING_ID = 73932198L;

    @Autowired
    ModelService modelService;
    @Autowired
    OpcuaInformationModelRepository opcuaInformationModelRepository;
    @Autowired
    DBFileStorageService dbFileStorageService;
    @Autowired
    OpcuaInformationModelService opcuaInformationModelService;

    private ModelMetadata testModelMetadata = DefaultDataBuilder.getNewMetadataBuilder().build();
    private Model testModel = Model.builder().modelMetadata(testModelMetadata).build();
    private DBFile testDbFile = DBFile.builder().fileName("testFile").fileType("testFileType").build();
    private OpcuaInformationModel testOpcuaInformationModel
            = OpcuaInformationModel.builder().dbFile(testDbFile).build();
    private OpcuaInformationModel testOpcuaInformationModel2 = OpcuaInformationModel.builder().dbFile(testDbFile).build();
    private MultipartFile testMultipartFile
            = new MockMultipartFile("data", "filename.txt", "text/xml",
            "some xml".getBytes());

    @BeforeEach
    public void setUp() throws IOException {
        testOpcuaInformationModel.setModel(testModel);
        given(opcuaInformationModelRepository.findById(EXISITNG_ID)).willReturn(Optional.of(testOpcuaInformationModel));
        given(opcuaInformationModelRepository.findById(SECOND_EXISITNG_ID)).willReturn(Optional.of(testOpcuaInformationModel2));
        given(opcuaInformationModelRepository.findAllByModelId(EXISITNG_MODEL_ID))
                .willReturn(Arrays.asList(testOpcuaInformationModel, testOpcuaInformationModel2));
        given(modelService.getModelForProjectById(EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID))
                .willReturn(Optional.of(testModel));
        given(modelService.getModelForProjectById(EXISITNG_PROJECT_ID, NON_EXISITNG_MODEL_ID))
                .willReturn(Optional.empty());
        given(dbFileStorageService.storeFile(testMultipartFile)).willReturn(testDbFile);
        given(opcuaInformationModelRepository.save(testOpcuaInformationModel)).willReturn(testOpcuaInformationModel);
        given(opcuaInformationModelRepository.save(testOpcuaInformationModel2)).willReturn(testOpcuaInformationModel2);
        doNothing().when(opcuaInformationModelRepository).deleteById(EXISITNG_ID);
        doNothing().when(opcuaInformationModelRepository).deleteById(SECOND_EXISITNG_ID);
        doNothing().when(dbFileStorageService).deleteFile(EXISITNG_ID);
    }

    @Test
    public void shouldGetOpcuaInformationModelById() {
        Optional<OpcuaInformationModel> optionalOpcuaInformationModel
                = opcuaInformationModelService.getOpcuaInformationModelById(EXISITNG_ID);
        assertEquals(Optional.of(testOpcuaInformationModel), optionalOpcuaInformationModel);
    }

    @Test
    public void shouldGetAllOpcuaInformationModels() {
        List<OpcuaInformationModel> opcuaInformationModels =
                opcuaInformationModelService.getAllOpcuaInformationModelsByModelAndProjectId(EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID);
        assertEquals(Arrays.asList(testOpcuaInformationModel, testOpcuaInformationModel2), opcuaInformationModels);
    }

    @Test
    public void shouldNotGetAnyInformationModelIdNotFound() {
        assertThrows(IdException.class,
                () -> opcuaInformationModelService.getAllOpcuaInformationModelsByModelAndProjectId(EXISITNG_PROJECT_ID, NON_EXISITNG_MODEL_ID));
    }

    @Test
    public void shouldGetOpcuaInformationModelRawById() {
        Optional<DBFile> dbFile = opcuaInformationModelService.getOpcuaInformationModelRaw(EXISITNG_ID);
        assertEquals(testDbFile, dbFile.get());
    }

    @Test
    public void shouldSaveOpcuaInformationModel() throws IOException {
        MultipartFile dummyMultipartFile = new MockMultipartFile("data", "filename.txt", "text/plain",
                this.getClass().getResourceAsStream("/informationModel_example.xml"));
        OpcuaInformationModel parsedOpcuaInformationModel =
                opcuaInformationModelService.parseOpcuaInformationModelRaw(dummyMultipartFile, null, null);
        parsedOpcuaInformationModel.setModel(testModel);
        given(opcuaInformationModelRepository.save(parsedOpcuaInformationModel)).willReturn(parsedOpcuaInformationModel);
        OpcuaInformationModel opcuaInformationModel = opcuaInformationModelService
                .saveOpcuaInformationModel(dummyMultipartFile, EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID, null, null);
        assertEquals("Machine", opcuaInformationModel.getOpcuaMetadata().getMachine().getDisplayName());
    }

    @Test
    public void shouldNotSaveOpcuaInformationModelNoFile() {
        given(modelService.getModelForProjectById(EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID)).willReturn(Optional.empty());
        assertThrows(IdException.class,
                () -> opcuaInformationModelService
                        .saveOpcuaInformationModel(testMultipartFile, EXISITNG_MODEL_ID, EXISITNG_PROJECT_ID, null, null));
    }

    @Test
    public void shouldNotSaveOpcuaInformationModelFailToSaveFile() throws IOException {
        given(dbFileStorageService.storeFile(testMultipartFile)).willThrow(new IOException());
        assertThrows(IllegalArgumentException.class,
                () -> opcuaInformationModelService
                        .saveOpcuaInformationModel(testMultipartFile, EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID, null, null));
    }

    @Test
    public void shouldParseOpcuaInformationModel() throws IOException {
        MultipartFile dummyMultipartFile = new MockMultipartFile("data", "filename.txt", "text/plain",
                this.getClass().getResourceAsStream("/informationModel_example.xml"));
        OpcuaInformationModel opcuaInformationModel = opcuaInformationModelService.parseOpcuaInformationModelRaw(dummyMultipartFile, null, null);
        assertEquals(opcuaInformationModel.getOpcuaMetadata().getMachine().getDisplayName(), "Machine");
    }

    @Test
    public void shouldNotParseOpcuaInformationModel() throws IOException {
        MultipartFile dummyMultipartFile = new MockMultipartFile("data", "filename.txt", "text/plain",
                this.getClass().getResourceAsStream("fail.xml"));
        assertThrows(IllegalArgumentException.class,
                () -> opcuaInformationModelService.parseOpcuaInformationModelRaw(dummyMultipartFile, null, null));
    }

    @Test
    public void shouldDeleteOpcuaInformationModel() {
        OpcuaInformationModel informationModel = new OpcuaInformationModel();
        informationModel.setId(EXISITNG_ID);
        testModel.setOpcuaInformationModels(Arrays.asList(informationModel));
        testModel.setId(EXISITNG_MODEL_ID);
        assertTrue(opcuaInformationModelService
                .deleteOpcuaInformationModel(EXISITNG_ID, EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID));
    }

    @Test
    public void shouldNotDeleteOpcuaInformationModelIdExceptionForModel() {
        assertThrows(IdException.class, () -> opcuaInformationModelService
                .deleteOpcuaInformationModel(EXISITNG_ID, EXISITNG_PROJECT_ID, NON_EXISITNG_MODEL_ID));
    }

    @Test
    public void shouldNotDeleteOpcuaInformationModelIdExceptionForOpcuaModel() {
        assertThrows(IdException.class, () -> opcuaInformationModelService
                .deleteOpcuaInformationModel(NON_EXISTING_ID, EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID));
    }

    @Test
    public void shouldDeleteAllOpcuaInformationModels() {
        OpcuaInformationModel informationModel = new OpcuaInformationModel();
        informationModel.setId(EXISITNG_ID);
        testModel.setOpcuaInformationModels(Arrays.asList(informationModel));
        testModel.setId(EXISITNG_MODEL_ID);
        assertTrue(opcuaInformationModelService
                .deleteAllOpcuaInformationModelsForModel(EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID));
    }

    @Test
    public void shouldNotDeleteAllOpcuaInformationModelsIdExceptionForModel() {
        assertThrows(IdException.class, () -> opcuaInformationModelService
                .deleteAllOpcuaInformationModelsForModel(EXISITNG_PROJECT_ID, NON_EXISITNG_MODEL_ID));
    }

    @Test
    public void shouldNotDeleteOpcuaInformationModelIdNotEqual() {
        testModel.setOpcuaInformationModels(Collections.singletonList(testOpcuaInformationModel));
        testOpcuaInformationModel.setModel(testModel);
        testOpcuaInformationModel.getModel().setId(EXISITNG_MODEL_ID);
        assertThrows(IllegalArgumentException.class, () ->{
            testModel.setId(NON_EXISITNG_MODEL_ID);
            opcuaInformationModelService.deleteOpcuaInformationModel(EXISITNG_ID, EXISITNG_PROJECT_ID, EXISITNG_MODEL_ID);
        });
    }

    @TestConfiguration
    static class OpcuaInformationModelServiceTestContextConfiguration {

        @MockBean
        public OpcuaInformationModelRepository opcuaInformationModelRepository;

        @MockBean
        public ModelService modelService;

        @MockBean
        public DBFileStorageService dbFileStorageService;

        @Bean
        public OpcuaInformationModelService opcuaInformationModelService() {
            return new OpcuaInformationModelServiceImpl
                    (this.opcuaInformationModelRepository, this.dbFileStorageService, this.modelService);
        }
    }
}
