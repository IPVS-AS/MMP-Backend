package de.unistuttgart.ipvs.as.mmp.model.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.*;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EntityScan(basePackages = "de.unistuttgart.ipvs.as.mmp.common.domain")
@EnableJpaRepositories(basePackages = "de.unistuttgart.ipvs.as.mmp.model.repository")
@Import(MmpJpaTestConfig.class)
public class ModelFIleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ModelFileRepository modelFileRepository;

    private Project testProject = Project.builder()
            .name("test project")
            .build();
    private ModelMetadata testModelMetadata = DefaultDataBuilder.getNewMetadataBuilder().author(null).build();
    private Model testModel = Model.builder().modelMetadata(testModelMetadata).project(testProject).build();


    private DBFile testDbFile = DBFile.builder().fileName("testFile").fileType("testFileType").build();
    private ModelFile testModelFile = ModelFile.builder().model(testModel).dbFile(testDbFile).build();

    @BeforeEach
    public void setUp() {
        modelFileRepository.deleteAll();
        entityManager.persist(testProject);
        entityManager.persist(testModel);
        entityManager.persist(testDbFile);
        entityManager.persist(testModelFile);
    }

    @Test
    public void shouldFindAll() {
        List<ModelFile> modelFiles = modelFileRepository.findAll();
        assertEquals(Collections.singletonList(testModelFile), modelFiles);
    }

    @Test
    public void shouldNotFindAny() {
        entityManager.remove(testModelFile);
        List<ModelFile> modelFiles = modelFileRepository.findAll();
        assertEquals(Collections.emptyList(), modelFiles);
    }

    @Test
    public void shouldNotFindById() {
        Optional<ModelFile> modelFile = modelFileRepository.findById(123456789L);
        assertEquals(Optional.empty(), modelFile);
    }

    @Test
    public void shouldFindById() {
        Optional<ModelFile> modelFile = modelFileRepository.findById(testModelFile.getId());
        assertEquals(Optional.of(testModelFile), modelFile);
    }
}
