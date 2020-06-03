package de.unistuttgart.ipvs.as.mmp.eam.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.exception.MMPExceptionHandler;
import de.unistuttgart.ipvs.as.mmp.eam.domain.OrganisationUnit;
import de.unistuttgart.ipvs.as.mmp.eam.service.OrganisationUnitService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@WebMvcTest(controllers = {OrganisationUnitController.class})
public class OrganisationUnitControllerTest extends ControllerTest {
    @MockBean
    OrganisationUnitService orgaUnitService;

    @Autowired
    OrganisationUnitController organisationUnitController;

    MockMvc rest;

    private static final String ALL_ORGA_UNITS_PATH = OrganisationUnitController.PATH;
    private static final Long ORGA_UNIT_ID = 123L;
    private static final String ORGA_UNIT_ID_PATH = ALL_ORGA_UNITS_PATH + "/" + ORGA_UNIT_ID;
    private static final String EXISTING_LABEL = "orga unit 1";
    private static final Integer EXISTING_INDEX = 0;

    private OrganisationUnit testOrgaUnit = new OrganisationUnit(EXISTING_LABEL, EXISTING_INDEX);

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.rest = MockMvcBuilders.standaloneSetup(this.organisationUnitController)
                .setControllerAdvice(new MMPExceptionHandler(), this.organisationUnitController)
                .apply(documentationConfiguration(restDocumentation).
                        operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @Test
    public void shouldFindOrgaUnitById() throws Exception {
        BDDMockito.given(orgaUnitService.getOrgaUnitById(ORGA_UNIT_ID))
                .willReturn(Optional.of(testOrgaUnit));
        rest.perform(MockMvcRequestBuilders.get(ORGA_UNIT_ID_PATH)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.label").value(EXISTING_LABEL))
                .andExpect(MockMvcResultMatchers.jsonPath("$.index").value(EXISTING_INDEX))
                .andDo(document("org-unit-get"));
    }

    @Test
    public void shouldNotFindOrgaUnitById() throws Exception {
        BDDMockito.given(orgaUnitService.getOrgaUnitById(ORGA_UNIT_ID))
                .willReturn(Optional.empty());
        rest.perform(MockMvcRequestBuilders.get(ORGA_UNIT_ID_PATH)).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldGetAllOrgaUnits() throws Exception {
        BDDMockito.given(orgaUnitService.getAllOrgaUnits())
                .willReturn(Arrays.asList(testOrgaUnit));
        rest.perform(MockMvcRequestBuilders.get(ALL_ORGA_UNITS_PATH)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andDo(document("org-unit-get-all"));
    }

    @Test
    public void shouldNotGetAnyOrgaUnit() throws Exception {
        BDDMockito.given(orgaUnitService.getAllOrgaUnits())
                .willReturn(Collections.emptyList());
        rest.perform(MockMvcRequestBuilders.get(ALL_ORGA_UNITS_PATH)).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));
    }

    @Test
    public void shouldUpdateOrgaUnitById() throws Exception {
        OrganisationUnit updatedOrgaUnit = new OrganisationUnit("updated orga unit", 1);
        BDDMockito.given(orgaUnitService.getOrgaUnitById(ORGA_UNIT_ID))
                .willReturn(Optional.of(testOrgaUnit));
        BDDMockito.given(orgaUnitService.updateOrgaUnitById(ORGA_UNIT_ID, updatedOrgaUnit))
                .willReturn(updatedOrgaUnit);
        rest.perform(MockMvcRequestBuilders.put(ORGA_UNIT_ID_PATH).contentType(APPLICATION_JSON).content(rawJson(updatedOrgaUnit)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(document("org-unit-update"));
    }

    @Test
    public void shouldNotUpdateOrgaUnitById() throws Exception {
    BDDMockito.given(orgaUnitService.updateOrgaUnitById(ArgumentMatchers.any(), ArgumentMatchers.any()))
           .willThrow(IdException.idNotFound(OrganisationUnit.class, ORGA_UNIT_ID));
    rest.perform(MockMvcRequestBuilders.put(ORGA_UNIT_ID_PATH).contentType(APPLICATION_JSON).content("{}"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldDeleteOrgaUnitById() throws Exception {
        BDDMockito.given(orgaUnitService.deleteOrgaUnitById(ORGA_UNIT_ID))
                .willReturn(true);
        rest.perform(MockMvcRequestBuilders.delete(ORGA_UNIT_ID_PATH))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(document("org-unit-delete"));
    }

    @Test
    public void shouldDeleteAllOrgaUnits() throws Exception {
        Mockito.doNothing().when(orgaUnitService).deleteAllOrgaUnits();
        rest.perform(MockMvcRequestBuilders.delete(ALL_ORGA_UNITS_PATH))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void shouldNotDeleteOrgaUnitById() throws Exception {
        BDDMockito.given(orgaUnitService.deleteOrgaUnitById(ORGA_UNIT_ID))
                .willReturn(false);
        rest.perform(MockMvcRequestBuilders.delete(ORGA_UNIT_ID_PATH))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldSaveOrgaUnitById() throws Exception {
        testOrgaUnit.setId(ORGA_UNIT_ID);
        BDDMockito.given(orgaUnitService.saveOrgaUnit(testOrgaUnit))
                .willReturn(testOrgaUnit);
        rest.perform(MockMvcRequestBuilders.post(ALL_ORGA_UNITS_PATH).contentType(APPLICATION_JSON).content(rawJson(testOrgaUnit)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrl(ORGA_UNIT_ID_PATH))
                .andDo(document("org-unit-create"));
    }

    @Test
    public void shouldNotSaveOrgaUnitById() throws Exception {
        BDDMockito.given(orgaUnitService.saveOrgaUnit(ArgumentMatchers.any()))
                .willThrow(new NullPointerException());
        rest.perform(MockMvcRequestBuilders.post(ALL_ORGA_UNITS_PATH).contentType(APPLICATION_JSON).content("{}"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }
}
