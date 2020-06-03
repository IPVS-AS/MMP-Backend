package de.unistuttgart.ipvs.as.mmp.search.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaObjectNode;
import de.unistuttgart.ipvs.as.mmp.search.repository.ModelSearchDao;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelService;
import de.unistuttgart.ipvs.as.mmp.search.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class SearchServiceTest {

    @Autowired
    ModelService modelService;
    @Autowired
    ModelSearchDao modelSearchDao;
    @Autowired
    SearchService searchService;

    private Model model1 = Model.builder().modelMetadata(ModelMetadata.builder().algorithm("Simple Logistic").build()).build();
    private Model model2 = Model.builder().modelMetadata(ModelMetadata.builder().build()).build();
    private Model model3 = Model.builder().modelMetadata(ModelMetadata.builder().algorithm("Linear Regression").build()).opcuaInformationModels(
            Collections.singletonList(OpcuaInformationModel.builder().opcuaMetadata(OpcuaMetadata.builder().machine(OpcuaObjectNode.builder().displayName("Machine").build()).build()).build())).build();
    private Model model4 = Model.builder().modelMetadata(ModelMetadata.builder().algorithm("Simple Logistic").build()).opcuaInformationModels(
            Collections.singletonList(OpcuaInformationModel.builder().opcuaMetadata(OpcuaMetadata.builder().sensors(Collections.singletonList(OpcuaObjectNode.builder().displayName("TemperaturSensor").build())).build()).build())).build();

    @BeforeEach
    public void setUp() {
        given(modelService.getAllModels()).willReturn(Arrays.asList(model1, model2, model3, model4));
    }

    @Test
    public void shouldCollectAllAlgorithms() {
        assertThat(searchService.collectAlgorithms(), containsInAnyOrder(Arrays.asList(
                "_null_", "Simple Logistic", "Linear Regression"
        ).toArray()));
    }

    @Test
    public void shouldCollectAllMachineNames() {
        assertThat(searchService.collectMachineNodes(), containsInAnyOrder(Arrays.asList(
                "_null_", "Machine"
        ).toArray()));
    }

    @Test
    public void shouldCollectAllSensorNames() {
        assertThat(searchService.collectSensorNodes(), containsInAnyOrder(Arrays.asList(
                "_null_", "TemperaturSensor"
        ).toArray()));
    }
    
    @TestConfiguration
    static class SearchServiceTestContextConfiguration {

        @MockBean
        public ModelSearchDao modelSearchDao;

        @MockBean
        public ModelService modelService;

        @Bean
        public SearchService searchService() {return new SearchServiceImpl(this.modelService, this.modelSearchDao); }
    }

}
