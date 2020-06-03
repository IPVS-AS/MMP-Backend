package de.unistuttgart.ipvs.as.mmp.common.pmml;

import de.unistuttgart.ipvs.as.mmp.common.domain.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PMMLMetadataParserTest {

    private static final String PATH_TO_PMML_TEST_FILES = "pmml/%s";

    private ClassLoader classLoader;
    private ModelMetadata testMetadata;
    private ModelMetadata testMetadata2;

    @BeforeAll
    public void setUp() {
        classLoader = this.getClass().getClassLoader();
        List<InputAttribute> inputAttributes = Arrays.asList(InputAttribute.builder().name("sepal_length").usageType("supplementary")
                        .intervals(Arrays.asList(new Interval(4.3, 7.9, "closedClosed"))).dataType("double").build(),
                InputAttribute.builder().name("sepal_width").usageType("supplementary").intervals(Arrays.asList(new Interval(2.0, 4.4, "closedClosed")))
                        .dataType("double").build(),
                InputAttribute.builder().name("petal_length").usageType("active").intervals(Arrays.asList(new Interval(1.0, 6.9, "closedClosed")))
                        .dataType("double").build(),
                InputAttribute.builder().name("petal_width").usageType("supplementary").intervals(Arrays.asList(new Interval(0.1, 2.5, "closedClosed")))
                        .dataType("double").build()
        );
        List<OutputAttribute> outputAttributes = Arrays.asList(new OutputAttribute("class", "predictedValue", "categorical"), new OutputAttribute("Probability_Iris-setosa", "probability", "continuous"),
                new OutputAttribute("Probability_Iris-versicolor", "probability", "continuous"), new OutputAttribute("Probability_Iris-virginica", "probability", "continuous"));
        testMetadata = ModelMetadata.builder()
                .name("RPart_Model").algorithm("rpart")
                .pmmlMetadata(PMMLMetadata.builder().pmmlVersion("4.3").description("RPart Decision Tree Model").applicationName("Rattle/PMML").applicationVersion("1.2.29").modelVersion(null).timestamp("2012-09-27 12:46:08").algorithmName("rpart").miningFunction("classification").inputAttributes(inputAttributes).outputAttributes(outputAttributes).build())
                .modelDescription("RPart Decision Tree Model")
                .build();

        testMetadata2 = ModelMetadata.builder()
                .name("Model 1").algorithm("Linear Regression")
                .pmmlMetadata(PMMLMetadata.builder().pmmlVersion("4.3").description("RPart Decision Tree Model").applicationName("Rattle/PMML").applicationVersion("1.2.29").modelVersion(null).timestamp("2012-09-27 12:46:08").algorithmName("rpart").miningFunction("classification").inputAttributes(inputAttributes).outputAttributes(outputAttributes).build())
                .modelDescription("Test Model")
                .build();
    }

    @Test
    public void testParsingDecisionTreeIris() throws IOException, JAXBException, SAXException {
        String pathToTestFile = String.format(PATH_TO_PMML_TEST_FILES, "iristree_example.xml");
        try (InputStream input = classLoader.getResourceAsStream(pathToTestFile)) {
            PMMLMetadataParser metadataParser = new PMMLMetadataParser();
            List<Model> modelList = metadataParser.parsePMMLFile(input, new ModelMetadata());
            assertEquals(1, modelList.size());
            Model model = modelList.get(0);
            assertEquals(testMetadata, model.getModelMetadata());
        }
    }

    @Test
    public void testParsingMultipleDecisionTrees() throws IOException, JAXBException, SAXException {
        String pathToTestFile = String.format(PATH_TO_PMML_TEST_FILES, "ensemble_audit_dectree.xml");
        try (InputStream input = classLoader.getResourceAsStream(pathToTestFile)) {
            PMMLMetadataParser metadataParser = new PMMLMetadataParser();
            List<Model> modelList = metadataParser.parsePMMLFile(input, new ModelMetadata());
            assertEquals(10, modelList.size());
            for (de.unistuttgart.ipvs.as.mmp.common.domain.Model model : modelList) {
                assertNotNull(model);
                ModelMetadata modelMetadata = model.getModelMetadata();
                assertNotNull(modelMetadata);
                assertNotNull(modelMetadata.getName());
            }
        }
    }

    @Test
    public void testParsingPMMLFileButNotOverrideFieldsInMetadata() throws IOException, JAXBException, SAXException {
        ModelMetadata inputMetadata = ModelMetadata.builder()
                .name("Model 1").algorithm("Linear Regression")
                .modelDescription("Test Model")
                .build();

        String pathToTestFile = String.format(PATH_TO_PMML_TEST_FILES, "iristree_example.xml");
        try (InputStream input = classLoader.getResourceAsStream(pathToTestFile)) {
            PMMLMetadataParser metadataParser = new PMMLMetadataParser();
            List<Model> modelList = metadataParser.parsePMMLFile(input, inputMetadata);
            assertEquals(1, modelList.size());
            Model model = modelList.get(0);
            assertEquals(testMetadata2, model.getModelMetadata());
        }
    }

    @Test
    public void testParsingNonPMMLFile() throws IOException {
        String pathToTestFile = String.format(PATH_TO_PMML_TEST_FILES, "nonpmml.xml");
        try (InputStream input = classLoader.getResourceAsStream(pathToTestFile)) {
            PMMLMetadataParser metadataParser = new PMMLMetadataParser();
            assertThrows(UnmarshalException.class, () -> metadataParser.parsePMMLFile(input, new ModelMetadata()));
        }
    }

}
