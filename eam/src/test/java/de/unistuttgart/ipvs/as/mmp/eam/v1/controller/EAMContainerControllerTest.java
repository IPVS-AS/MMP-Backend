package de.unistuttgart.ipvs.as.mmp.eam.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.exception.MMPExceptionHandler;
import de.unistuttgart.ipvs.as.mmp.eam.domain.BusinessProcess;
import de.unistuttgart.ipvs.as.mmp.eam.domain.EAMContainer;
import de.unistuttgart.ipvs.as.mmp.eam.domain.OrganisationUnit;
import de.unistuttgart.ipvs.as.mmp.eam.service.EAMContainerService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@WebMvcTest(controllers = {EAMContainerController.class})
public class EAMContainerControllerTest extends ControllerTest {
    @MockBean
    private EAMContainerService eamContainerService;

    @Autowired
    private EAMContainerController eamContainerController;

    MockMvc rest;

    private static final String EAM_CONTAINER_PATH = EAMContainerController.PATH;
    private static final String ALL_EAM_CONTAINER_PATH = EAM_CONTAINER_PATH + "/all";
    private static final Long EXISTING_EAM_CONTAINER_ID = 1L;
    private static final Long NON_EXISTING_EAM_CONTAINER_ID = 12345L;
    private static final Long EXISTING_MODEL_ID = 123L;
    private static final String EAM_CONTAINER_ID_PATH = EAM_CONTAINER_PATH + "/" + EXISTING_EAM_CONTAINER_ID;
    private static final String EAM_BY_MODEL_ID_PATTERN = EAM_CONTAINER_PATH + "/models/" + EXISTING_MODEL_ID;

    private Model testModel = Model.builder().build();
    private BusinessProcess testBusinessProcess = BusinessProcess.builder().label("testBusinessProcess").build();
    private OrganisationUnit testOrgUnit = OrganisationUnit.builder().label("testOrgaUnit").index(0).build();
    private EAMContainer testEamContainer = EAMContainer.builder()
            .businessProcess(testBusinessProcess)
            .organisationUnit(testOrgUnit)
            .model(testModel).build();

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.rest = MockMvcBuilders.standaloneSetup(this.eamContainerController)
                .setControllerAdvice(new MMPExceptionHandler(), this.eamContainerController)
                .apply(documentationConfiguration(restDocumentation).
                        operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
        this.testModel.setId(EXISTING_MODEL_ID);
        this.testBusinessProcess.setId(124L);
        this.testOrgUnit.setId(125L);
    }

    @Test
    public void shouldGetAllEAMContainers() throws Exception {
        given(eamContainerService.getAllEAMContainers()).willReturn(Collections.singletonList(testEamContainer));
        rest.perform(MockMvcRequestBuilders.get(EAM_CONTAINER_PATH)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andDo(document("eam-container-get-all"));
    }

    @Test
    public void shouldNotGetAllEAMContainers() throws Exception {
        given(eamContainerService.getAllEAMContainers()).willReturn(Collections.emptyList());
        rest.perform(MockMvcRequestBuilders.get(EAM_CONTAINER_PATH)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));
    }

    @Test
    public void shouldGetAllEAMContainersByModelId() throws Exception {
        given(eamContainerService.getAllEAMContainersByModelId(testModel.getId()))
                .willReturn(Collections.singletonList(testEamContainer));
        rest.perform(MockMvcRequestBuilders.get(EAM_BY_MODEL_ID_PATTERN)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andDo(document("eam-container-get-by-model"));
    }

    @Test
    public void shouldGetNoEAMContainerByModelId() throws Exception {
        given(eamContainerService.getAllEAMContainersByModelId(testModel.getId()))
                .willReturn(Collections.emptyList());
        rest.perform(MockMvcRequestBuilders.get(EAM_BY_MODEL_ID_PATTERN)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));
    }

    @Test
    public void shouldThrowExceptionEAMContainerByModelId() throws Exception {
        given(eamContainerService.getAllEAMContainersByModelId(testModel.getId()))
                .willReturn(null);
        rest.perform(MockMvcRequestBuilders.get(EAM_BY_MODEL_ID_PATTERN))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    public void shouldUpdateEAMContainer() throws Exception {
        given(eamContainerService.updateEAMContainerById(EXISTING_EAM_CONTAINER_ID, testEamContainer)).willReturn(testEamContainer);
        rest.perform(MockMvcRequestBuilders.put(EAM_CONTAINER_ID_PATH).contentType(APPLICATION_JSON).content(rawJson(testEamContainer)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(document("eam-container-update"));
    }

    @Test
    public void shouldNotUpdateEAMContainer() throws Exception {
        given(eamContainerService.updateEAMContainerById(EXISTING_EAM_CONTAINER_ID, testEamContainer)).willReturn(null);
        rest.perform(MockMvcRequestBuilders.put(EAM_CONTAINER_ID_PATH).contentType(APPLICATION_JSON).content(rawJson(testEamContainer)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldSaveEAMContainer() throws Exception {
        testEamContainer.setId(EXISTING_EAM_CONTAINER_ID);
        given(eamContainerService.saveEAMContainer(testEamContainer)).willReturn(testEamContainer);
        rest.perform(MockMvcRequestBuilders.post(EAM_CONTAINER_PATH).contentType(APPLICATION_JSON).content(rawJson(testEamContainer)))
                .andDo(document("eam-container-create"));
    }

    @Test
    public void shouldSaveEAMContainers() throws Exception {
        testEamContainer.setId(EXISTING_EAM_CONTAINER_ID);
        given(eamContainerService.saveEAMContainers(Collections.singletonList(testEamContainer))).willReturn(Collections.singletonList(testEamContainer));
        rest.perform(MockMvcRequestBuilders.post(ALL_EAM_CONTAINER_PATH).contentType(APPLICATION_JSON).content(rawJson(Collections.singletonList(testEamContainer))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrl(EAM_CONTAINER_PATH));
    }

    @Test
    public void shouldDeleteEAMContainer() throws Exception {
        given(eamContainerService.deleteEAMContainer(EXISTING_EAM_CONTAINER_ID)).willReturn(true);
        rest.perform(MockMvcRequestBuilders.delete(EAM_CONTAINER_ID_PATH))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(document("eam-container-delete-all"));
    }

    @Test
    public void shouldNotDeleteEAMContainer() throws Exception {
        given(eamContainerService.deleteEAMContainer(EXISTING_EAM_CONTAINER_ID)).willReturn(false);
        rest.perform(MockMvcRequestBuilders.delete(EAM_CONTAINER_ID_PATH))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldDeleteAllEAMContainer() throws Exception {
        doNothing().when(eamContainerService).deleteAllEAMContainer();
        rest.perform(MockMvcRequestBuilders.delete(EAM_CONTAINER_PATH))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

}
