package de.unistuttgart.ipvs.as.mmp.search.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.exception.MMPExceptionHandler;
import de.unistuttgart.ipvs.as.mmp.search.domain.dto.SearchDto;
import de.unistuttgart.ipvs.as.mmp.search.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@WebMvcTest(controllers = {SearchController.class})
public class SearchControllerTest extends ControllerTest {

    private static final String SEARCH_PATH = "/v1/advsearch";

    MockMvc rest;
    @MockBean
    private SearchService searchService;

    @Autowired
    private SearchController searchController;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.rest = MockMvcBuilders.standaloneSetup(this.searchController)
                .setControllerAdvice(new MMPExceptionHandler(), this.searchController)
                .apply(documentationConfiguration(restDocumentation).
                        operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @Test
    public void shouldFindAlgorithmsToFilter() throws Exception {
        List<String> algorithms = Arrays.asList("_null_", "Linear Regression", "Simple Logistic");
        given(searchService.collectAlgorithms())
                .willReturn(algorithms);
        rest.perform(get(SEARCH_PATH))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect((jsonPath("$.possibleAlgorithmsToFilter[0]").value(algorithms.get(0))))
                .andExpect(jsonPath("$.possibleAlgorithmsToFilter", hasSize(3)))
                .andDo(document("filter"));
    }

    @Test
    public void shouldFindMachienNamesToFilter() throws Exception {
        List<String> machineNames = Arrays.asList("_null_", "Machine", "Machine2");
        given(searchService.collectMachineNodes())
                .willReturn(machineNames);
        rest.perform(get(SEARCH_PATH))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect((jsonPath("$.possibleMachineNamesToFilter[0]").value(machineNames.get(0))))
                .andExpect(jsonPath("$.possibleMachineNamesToFilter", hasSize(3)));
    }

    @Test
    public void shouldFindSensorNamesToFilter() throws Exception {
        List<String> sensorNames = Arrays.asList("_null_", "LichtSensor", "TemperaturSensor");
        given(searchService.collectSensorNodes())
                .willReturn(sensorNames);
        rest.perform(get(SEARCH_PATH))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect((jsonPath("$.possibleSensorNamesToFilter[0]").value(sensorNames.get(0))))
                .andExpect(jsonPath("$.possibleSensorNamesToFilter", hasSize(3)));
    }

    @Test
    public void shouldFilter() throws Exception {
        List<String> algorithmToFilter = Arrays.asList("Linear Regression","Simple Logistic");

        List<Model> models = Arrays.asList(
                Model.builder().modelMetadata(ModelMetadata.builder().algorithm("Linear Regression").build()).build(),
                Model.builder().modelMetadata(ModelMetadata.builder().algorithm("Simple Logistic").build()).build()
        );

        given(searchService.getModelsByFiltersAndSearchTerms(null, algorithmToFilter, null, null))
                .willReturn(models);

        rest.perform(post(SEARCH_PATH).contentType(MediaType.APPLICATION_JSON).content(json(SearchDto.builder().algorithmsToFilterFor(algorithmToFilter).build())))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andDo(document("search"));
    }

}
