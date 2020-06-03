package de.unistuttgart.ipvs.as.mmp.search.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.*;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaObjectNode;
import de.unistuttgart.ipvs.as.mmp.common.opcua.OpcuaMetadataParser;
import de.unistuttgart.ipvs.as.mmp.common.util.DefaultDataBuilder;
import de.unistuttgart.ipvs.as.mmp.search.configuration.HibernateSearchConfig;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.opcfoundation.ua._2011._03.uanodeset.ObjectFactory;
import org.opcfoundation.ua._2011._03.uanodeset.UANodeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {HibernateSearchConfig.class},
        loader = AnnotationConfigContextLoader.class)
@Transactional
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModelSearchDaoIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ModelSearchDao modelSearchDao;

    private List<Model> models;

    @Commit
    @Before
    public void setupTestData() {
        User user1 = User.builder().name("Max Mustermann").build();
        User user2 = User.builder().name("Horst").build();

        Project project1 = Project.builder().name("TestProjekt1").editors(Collections.singletonList(user1)).build();
        Project project2 = Project.builder().name("TestProjekt2").editors(Collections.singletonList(user2)).build();

        ModelMetadata testMetadata = DefaultDataBuilder.getNewMetadataBuilder()
                .name("Best Model")
                .algorithm("J48")
                .modelDescription("description")
                .author(user1)
                .build();

        ModelMetadata testMetadata2 = DefaultDataBuilder.getNewMetadataBuilder()
                .name("Model2")
                .author(user2)
                .algorithm("Simple Logistic")
                .modelDescription("This is a simple model!")
                .build();

        ModelMetadata testMetadata3 = DefaultDataBuilder.getNewMetadataBuilder()
                .name("Model3")
                .author(user2)
                .algorithm("Simple Logistic")
                .modelDescription("description")
                .build();

        RelationalDBInformation relationalDBInformation = RelationalDBInformation.builder()
                .url("http://test").dbUser("testUser").password("secret").build();
        OpcuaMetadata opcuaMetadata = parseOpcua();

        Model model1 = Model.builder().project(project1).modelMetadata(testMetadata).relationalDBInformation(relationalDBInformation).build();
        Model model2 = Model.builder().project(project2).modelMetadata(testMetadata2).build();
        Model model3 = Model.builder().project(project2).modelMetadata(testMetadata3).build();

        DBFile testDbFile = DBFile.builder().fileName("testFile").fileType("testFileType").build();
        OpcuaInformationModel testOpcuaInformationModel
                = OpcuaInformationModel.builder().dbFile(testDbFile).opcuaMetadata(opcuaMetadata).build();
        testOpcuaInformationModel.setModel(model3);
        model3.setOpcuaInformationModels(Collections.singletonList(testOpcuaInformationModel));

        project1.setModels(Collections.singletonList(model1));
        project2.setModels(Arrays.asList(model2, model3));

        relationalDBInformation.setModel(model1);

        models = Arrays.asList(model1, model2, model3);
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(model1);
        entityManager.persist(model2);
        entityManager.persist(model3);
        entityManager.persist(testOpcuaInformationModel);

    }

    public OpcuaMetadata parseOpcua() {
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("informationModel_example.xml");
            JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
            UANodeSet nodeSet = (UANodeSet) jc.createUnmarshaller().unmarshal(inputStream);
            OpcuaMetadata opcuaMetadata = new OpcuaMetadataParser(nodeSet).getOpcuaMetadata();
            opcuaMetadata.getMachine().setProperties(null);
            opcuaMetadata.getMachine().setComponents(null);
            opcuaMetadata.getMachine().setVariables(null);
            for (OpcuaObjectNode sensor : opcuaMetadata.getSensors()) {
                sensor.setVariables(null);
                sensor.setComponents(null);
                sensor.setProperties(null);
            }
            return opcuaMetadata;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Commit
    @Test
    public void testA_whenInitialTestDataInserted_thenSuccess() {
        for (int i = 0; i < models.size() - 1; i++) {
            entityManager.persist(models.get(i));
        }
    }

    @Test
    public void testB_whenIndexInitialized_thenCorrectIndexSize() throws InterruptedException {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        fullTextEntityManager.createIndexer()
                .startAndWait();
        int indexSize = fullTextEntityManager.getSearchFactory()
                .getStatistics()
                .getNumberOfIndexedEntities(Model.class.getName());

        assertEquals(models.size(), indexSize);
    }


    @Test
    public void testC_AlgorithmFilter() {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        try {
            fullTextEntityManager.createIndexer()
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> expectedModelNames1 = Collections.singletonList(models.get(0).getModelMetadata().getName());
        List<String> expectedModelNames2 = Arrays.asList(models.get(1).getModelMetadata().getName(), models.get(2).getModelMetadata().getName());
        List<String> expectedModelNames3 = Arrays.asList(models.get(0).getModelMetadata().getName(),
                models.get(1).getModelMetadata().getName(), models.get(2).getModelMetadata().getName());


        List<Model> resultsModels1 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                null, Collections.singletonList("J48"), null, null);
        List<String> results1ModelNames1 = resultsModels1.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());

        List<Model> resultsModels2 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                null, Collections.singletonList("Simple Logistic"), null, null);
        List<String> resultsModelNames2 = resultsModels2.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());

        List<Model> resultsModels3 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                null, Arrays.asList("J48", "Simple Logistic"), null, null);
        List<String> resultsModelNames3 = resultsModels3.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());

        assertThat(results1ModelNames1, containsInAnyOrder(expectedModelNames1.toArray()));
        assertThat(resultsModelNames2, containsInAnyOrder(expectedModelNames2.toArray()));
        assertThat(resultsModelNames3, containsInAnyOrder(expectedModelNames3.toArray()));
    }

    @Test
    public void testE_MachineNameFilter() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        try {
            fullTextEntityManager.createIndexer()
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> expectedModelNames1 = Collections.singletonList(models.get(2).getModelMetadata().getName());
        List<String> expectedModelNames2 = Collections.singletonList(models.get(1).getModelMetadata().getName());

        List<Model> resultsModels1 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                null, null, Collections.singletonList("Machine"), null);
        List<String> resultsModelNames1 = resultsModels1.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());

        List<Model> resultsModels2 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                null, null, Collections.singletonList("_null_"), null);
        List<String> resultsModelNames2 = resultsModels2.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());


        assertThat(resultsModelNames1, containsInAnyOrder(expectedModelNames1.toArray()));
        assertThat(resultsModelNames2, containsInAnyOrder(expectedModelNames2.toArray()));
    }

    @Test
    public void testF_SensorNameFilter() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        try {
            fullTextEntityManager.createIndexer()
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> expectedModelNames1 = Collections.singletonList(models.get(2).getModelMetadata().getName());
        List<String> expectedModelNames2 = Collections.singletonList(models.get(1).getModelMetadata().getName());

        List<Model> resultsModels1 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                null, null, null, Collections.singletonList("TemperaturSensor"));
        List<String> resultsModelNames1 = resultsModels1.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());

        List<Model> resultsModels2 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                null, null, null, Collections.singletonList("_null_"));
        List<String> resultsModelNames2 = resultsModels2.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());


        assertThat(resultsModelNames1, containsInAnyOrder(expectedModelNames1.toArray()));
        assertThat(resultsModelNames2, containsInAnyOrder(expectedModelNames2.toArray()));
    }

    @Test
    public void testG_SearchQuery() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        try {
            fullTextEntityManager.createIndexer()
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> expectedModelNames1 = Collections.singletonList(models.get(0).getModelMetadata().getName());
        List<String> expectedModelNames2 = Collections.singletonList(models.get(1).getModelMetadata().getName());
        List<String> expectedModelNames3 = Collections.singletonList(models.get(2).getModelMetadata().getName());

        List<Model> resultsModels1 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                Collections.singletonList("Best Model"), null, null, null);
        List<String> resultsModelNames1 = resultsModels1.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());

        List<Model> resultsModels2 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                Arrays.asList("Model2", "this is a simple model!"), null, null, null);
        List<String> resultsModelNames2 = resultsModels2.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());

        List<Model> resultsModels3 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                Arrays.asList("Model3", "description", "85.6"), null, null, null);
        List<String> resultsModelNames3 = resultsModels3.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());


        assertThat(resultsModelNames1, containsInAnyOrder(expectedModelNames1.toArray()));
        assertThat(resultsModelNames2, containsInAnyOrder(expectedModelNames2.toArray()));
        assertThat(resultsModelNames3, containsInAnyOrder(expectedModelNames3.toArray()));
    }

    @Test
    public void testH_SearchQueryAndFilters() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        try {
            fullTextEntityManager.createIndexer()
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> expectedModelNames1 = Collections.singletonList(models.get(1).getModelMetadata().getName());
        List<String> expectedModelNames2 = Collections.singletonList(models.get(2).getModelMetadata().getName());
        List<String> expectedModelNames3 = Arrays.asList(models.get(1).getModelMetadata().getName(), models.get(2).getModelMetadata().getName());

        List<Model> resultsModels1 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                Collections.singletonList("Model2"), null, Collections.singletonList("_null_"), null);
        List<String> resultsModelNames1 = resultsModels1.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());

        List<Model> resultsModels2 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                Collections.singletonList("Model3"), null, Collections.singletonList("Machine"), null);
        List<String> resultsModelNames2 = resultsModels2.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());

        List<Model> resultsModels3 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                Collections.singletonList("Model"), null, Arrays.asList("_null_", "Machine"), null);
        List<String> resultsModelNames3 = resultsModels3.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());


        assertThat(resultsModelNames1, containsInAnyOrder(expectedModelNames1.toArray()));
        assertThat(resultsModelNames2, containsInAnyOrder(expectedModelNames2.toArray()));
        assertThat(resultsModelNames3, containsInAnyOrder(expectedModelNames3.toArray()));
    }

    @Test
    public void testI_SearchQuerySubstring() {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        try {
            fullTextEntityManager.createIndexer()
                    .startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> expectedModelNames1 = Collections.singletonList(models.get(0).getModelMetadata().getName());
        List<String> expectedModelNames2 = Arrays.asList(models.get(0).getModelMetadata().getName(),
                models.get(1).getModelMetadata().getName(), models.get(2).getModelMetadata().getName());

        List<Model> resultsModels1 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                Collections.singletonList("est Mod"), null, null, null);
        List<String> resultsModelNames1 = resultsModels1.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());

        List<Model> resultsModels2 = modelSearchDao.getModelsByFiltersAndSearchTerms(
                Collections.singletonList("model"), null, null, null);
        List<String> resultsModelNames2 = resultsModels2.stream().map(model -> model.getModelMetadata().getName()).collect(Collectors.toList());

        assertThat(resultsModelNames1, containsInAnyOrder(expectedModelNames1.toArray()));
        assertThat(resultsModelNames2, containsInAnyOrder(expectedModelNames2.toArray()));
    }

}
