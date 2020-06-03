package de.unistuttgart.ipvs.as.mmp.scoring.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.*;
import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.Scoring;
import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.ScoringInput;
import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.ScoringOutput;
import de.unistuttgart.ipvs.as.mmp.common.pmml.PMMLMetadataParser;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelService;
import de.unistuttgart.ipvs.as.mmp.scoring.repository.ScoringRepository;
import de.unistuttgart.ipvs.as.mmp.scoring.service.impl.ScoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class ScoringServiceTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final Long PRPJECT_ID = 123L;
    private static final Long MODEL_ID = 123L;

    @Autowired
    private ScoringRepository scoringRepository;

    @Autowired
    private ModelService modelService;

    @Autowired
    private ScoringService scoringService;

    @TestConfiguration
    static class ScoringServiceTestConfiguration {

        @MockBean
        public ScoringRepository scoringRepository;

        @MockBean
        public ModelService modelService;

        @Bean
        public ScoringService scoringService() {
            return new ScoringServiceImpl(this.modelService, this.scoringRepository);
        }
    }

    private Project testProject = Project.builder().name("TestProject").build();
    private Model testModel = Model.builder().project(testProject).modelMetadata(new ModelMetadata()).build();
    private ModelFile modelFile = new ModelFile();
    private DBFile dbFile = new DBFile();
    private List<ScoringInput> inputs;
    private Scoring testScoring = new Scoring();

    private static final String expectedClass = "Iris-versicolor";
    private static final String expectedProbSetosa = "0.0";
    private static final String expectedProbVersicolor = "0.918918918918919";
    private static final String expectedProbVirginica = "0.08108108108108109";
    private PMMLMetadataParser pmmlMetadataParser = new PMMLMetadataParser();


    @BeforeEach
    public void setUp() {
        inputs = new ArrayList<>();
        inputs.add(new ScoringInput("petal_length", "3"));
        inputs.add(new ScoringInput("petal_width", "3"));
        inputs.add(new ScoringInput("sepal_length", "3"));
        inputs.add(new ScoringInput("sepal_width", "3"));

        dbFile.setFileName("iristree_example");
        dbFile.setFileType("xml");
        InputStream fs = null;
        try {
            File file = ResourceUtils.getFile("classpath:example/iristree_example.xml");
            dbFile.setFileName(file.getName());
            dbFile.setFileType("text/xml");
            dbFile.setData(Files.readAllBytes(file.toPath()));
            fs = new FileInputStream(file);
            List<Model> modelList = pmmlMetadataParser.parsePMMLFile(fs, testModel.getModelMetadata());
            testModel.setModelMetadata(modelList.get(0).getModelMetadata());
        } catch (Exception e) {
            log.info("Failed to load example pmml file, {}", e);
        } finally {
            try {
                fs.close();
            } catch (Exception e) {
                log.info("Failed to close input stream", e);
            }
        }
        modelFile.setDbFile(dbFile);
        modelFile.setModel(testModel);
        testModel.setModelFile(modelFile);

        given(modelService.getModelForProjectById(PRPJECT_ID, MODEL_ID)).willReturn(Optional.of(testModel));
        given(scoringRepository.save(testScoring)).willReturn(null);
    }

    @Test
    public void setTestScoring() {
        List<ScoringOutput> outputs = scoringService.scoreModel(PRPJECT_ID, MODEL_ID, inputs);
        assertEquals(4, outputs.size());
        assertEquals(expectedClass, outputs.get(0).getValue());
        assertEquals(expectedProbSetosa, outputs.get(1).getValue());
        assertEquals( expectedProbVersicolor, outputs.get(2).getValue());
        assertEquals( expectedProbVirginica, outputs.get(3).getValue());
    }
}
