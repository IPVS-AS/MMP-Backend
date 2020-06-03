package de.unistuttgart.ipvs.as.mmp.eam.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.exception.MMPExceptionHandler;
import de.unistuttgart.ipvs.as.mmp.eam.domain.BusinessProcess;
import de.unistuttgart.ipvs.as.mmp.eam.service.BusinessProcessService;
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
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;


@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@WebMvcTest(controllers = {BusinessProcessController.class})
public class BusinessProcessControllerTest extends ControllerTest{

    @MockBean
    private BusinessProcessService bpService;

    @Autowired
    private BusinessProcessController bpController;

    MockMvc rest;

    private BusinessProcess testBP = new BusinessProcess(null, "business process");

    private static final String ALL_BUSINESS_PROCESSES_PATH = BusinessProcessController.PATH;
    private static final Long BP_ID = 12345L;
    private static final String BP_ID_PATH = ALL_BUSINESS_PROCESSES_PATH +"/"+ BP_ID;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.rest = MockMvcBuilders.standaloneSetup(this.bpController)
                .setControllerAdvice(new MMPExceptionHandler(), this.bpController)
                .apply(documentationConfiguration(restDocumentation).
                        operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @Test
    public void shouldFindBusinessProcessById() throws Exception {
        BDDMockito.given(bpService.getBusinessProcessById(BP_ID)).willReturn(Optional.of(testBP));
        rest.perform(MockMvcRequestBuilders.get(BP_ID_PATH))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.label").value(testBP.getLabel()))
                .andDo(document("business-process-get"));
    }

    @Test
    public void shouldNotFindBusinessProcessById() throws Exception {
        BDDMockito.given(bpService.getBusinessProcessById(BP_ID)).willReturn(Optional.empty());
        rest.perform(MockMvcRequestBuilders.get(BP_ID_PATH))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldGetAllBusinessProcesses() throws Exception {
        BDDMockito.given(bpService.getAllBusinessProcesses()).willReturn(Collections.singletonList(testBP));
        rest.perform(MockMvcRequestBuilders.get(ALL_BUSINESS_PROCESSES_PATH))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                .andDo(document("business-process-get-all"));
    }

    @Test
    public void shouldNotGetAnyBusinessProcesses() throws Exception {
        BDDMockito.given(bpService.getAllBusinessProcesses()).willReturn(Collections.emptyList());
        rest.perform(MockMvcRequestBuilders.get(ALL_BUSINESS_PROCESSES_PATH))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));
    }

    @Test
    public void shouldUpdateBusinessProcess() throws Exception{
        BusinessProcess updatedBP = new BusinessProcess();
        updatedBP.setLabel("updated");
        BDDMockito.given(bpService.updateBusinessProcessById(BP_ID, updatedBP))
                .willReturn(updatedBP);
        rest.perform(MockMvcRequestBuilders.put(BP_ID_PATH).contentType(APPLICATION_JSON)
        .content(rawJson(updatedBP)))
                .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.label").value(updatedBP.getLabel()))
                .andDo(document("business-process-update"));
    }

    @Test
    public void shouldNotUpdateBusinessProcess() throws Exception{

        BDDMockito.given(bpService.updateBusinessProcessById(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .willThrow(IdException.idNotFound(BusinessProcess.class, BP_ID));
        rest.perform(MockMvcRequestBuilders.put(BP_ID_PATH).contentType(APPLICATION_JSON).content("{}"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldDeleteAllBusinessProcesses() throws Exception {
       Mockito.doNothing().when(bpService).deleteAllBusinessProcesses();
       rest.perform(MockMvcRequestBuilders.delete(ALL_BUSINESS_PROCESSES_PATH))
               .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void shouldDeleteBusinessProcessById() throws Exception {
        BDDMockito.given(bpService.deleteBusinessProcessById(BP_ID)).willReturn(true);
        rest.perform(MockMvcRequestBuilders.delete(BP_ID_PATH))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(document("business-process-delete"));
    }

    @Test
    public void shouldNotDeleteBusinessProcessById() throws Exception {
        BDDMockito.given(bpService.deleteBusinessProcessById(BP_ID)).willReturn(false);
        rest.perform(MockMvcRequestBuilders.delete(BP_ID_PATH))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldSaveBusinessProcess() throws Exception {
        testBP.setId(BP_ID);
        BDDMockito.given(bpService.saveBusinessProcesses(Collections.singletonList(testBP))).willReturn(Collections.singletonList(testBP));
        rest.perform(MockMvcRequestBuilders.post(ALL_BUSINESS_PROCESSES_PATH).contentType(APPLICATION_JSON).content(rawJson(Collections.singletonList(testBP))))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrl(BP_ID_PATH));
    }

    @Test
    public void shouldSaveBusinessProcesses() throws Exception {
        testBP.setId(BP_ID);

        BusinessProcess testBP2 = BusinessProcess.builder().label("test2").build();
        BusinessProcess testBP2Exp = BusinessProcess.builder().label("test2").predecessor(testBP).build();
        testBP2.setId(2L);
        testBP2Exp.setId(2L);

        List<BusinessProcess> businessProcessesExp = Arrays.asList(testBP, testBP2Exp);

        BDDMockito.given(bpService.saveBusinessProcesses(Arrays.asList(testBP, testBP2))).willReturn(businessProcessesExp);
        rest.perform(MockMvcRequestBuilders.post(ALL_BUSINESS_PROCESSES_PATH).contentType(APPLICATION_JSON).content(rawJson(Arrays.asList(testBP, testBP2))))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.redirectedUrl(ALL_BUSINESS_PROCESSES_PATH))
                .andDo(document("business-process-create"));
    }

    @Test
    public void shouldNotSaveBusinessProcess() throws Exception {
        BDDMockito.given(bpService.saveBusinessProcess(ArgumentMatchers.any()))
                .willThrow(NullPointerException.class);
        rest.perform(MockMvcRequestBuilders.post(ALL_BUSINESS_PROCESSES_PATH).contentType(APPLICATION_JSON).content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
