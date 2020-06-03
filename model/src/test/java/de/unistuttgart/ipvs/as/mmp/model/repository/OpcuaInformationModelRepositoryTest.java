package de.unistuttgart.ipvs.as.mmp.model.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.Project;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
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

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EntityScan(basePackages = "de.unistuttgart.ipvs.as.mmp.common.domain")
@EnableJpaRepositories(basePackages = "de.unistuttgart.ipvs.as.mmp.model.repository")
@Import(MmpJpaTestConfig.class)
public class OpcuaInformationModelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OpcuaInformationModelRepository opcuaInformationModelRepository;

    private static final Long NON_EXISTING_MODEL_ID = 124341L;

    private Project testProject = Project.builder()
            .name("test project")
            .build();
    private ModelMetadata testModelMetadata = DefaultDataBuilder.getNewMetadataBuilder().author(null).build();
    private Model testModel = Model.builder().modelMetadata(testModelMetadata).project(testProject).build();


    private DBFile testDbFile = DBFile.builder().fileName("testFile").fileType("testFileType").build();
    private OpcuaInformationModel testOpcuaInformationModel
            = OpcuaInformationModel.builder().dbFile(testDbFile).build();
    private OpcuaInformationModel testOpcuaInformationModel2
            = OpcuaInformationModel.builder().dbFile(testDbFile).build();

    @BeforeEach
    public void setUp() {
        testOpcuaInformationModel.setModel(testModel);
        testOpcuaInformationModel2.setModel(testModel);
        opcuaInformationModelRepository.deleteAll();
        entityManager.persist(testProject);
        entityManager.persist(testModel);
        entityManager.persist(testOpcuaInformationModel2);
    }

    @Test
    public void shouldFindAll() {
        List<OpcuaInformationModel> opcuaInformationModels = opcuaInformationModelRepository.findAll();
        assertEquals(Collections.singletonList(testOpcuaInformationModel), opcuaInformationModels);
    }

    @Test
    public void shouldNotFindAny() {
        entityManager.remove(testOpcuaInformationModel);
        entityManager.remove(testOpcuaInformationModel2);
        List<OpcuaInformationModel> opcuaInformationModels = opcuaInformationModelRepository.findAll();
        assertEquals(Collections.emptyList(), opcuaInformationModels);
    }

    @Test
    public void shouldNotFindById() {
        Optional<OpcuaInformationModel> optionalOpcuaInformationModel
                = opcuaInformationModelRepository.findById(NON_EXISTING_MODEL_ID);
        assertEquals(Optional.empty(), optionalOpcuaInformationModel);
    }

    @Test
    public void shouldFindById() {
        Optional<OpcuaInformationModel> optionalOpcuaInformationModel
                = opcuaInformationModelRepository.findById(testOpcuaInformationModel2.getId());
        assertEquals(Optional.of(testOpcuaInformationModel), optionalOpcuaInformationModel);
    }

    @Test
    public void shouldFindOneByModelId() {
        Long modelId = (Long) entityManager.getId(testModel);
        List<OpcuaInformationModel> opcuaInformationModels = opcuaInformationModelRepository.findAllByModelId(modelId);
        assertEquals(Collections.singletonList(testOpcuaInformationModel), opcuaInformationModels);
    }

    @Test
    public void shouldFindTwoModelId() {
        entityManager.persist(testOpcuaInformationModel);
        Long modelId = (Long) entityManager.getId(testModel);
        List<OpcuaInformationModel> opcuaInformationModels = opcuaInformationModelRepository.findAllByModelId(modelId);
        assertEquals(Arrays.asList(testOpcuaInformationModel, testOpcuaInformationModel2), opcuaInformationModels);
    }

    @Test
    public void shouldNotFindAnyByModelId() {
        List<OpcuaInformationModel> allByModelId = opcuaInformationModelRepository.findAllByModelId(NON_EXISTING_MODEL_ID);
        assertEquals(Collections.emptyList(), allByModelId);
    }

}
