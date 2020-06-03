package de.unistuttgart.ipvs.as.mmp.eam.service;

import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.eam.domain.BusinessProcess;
import de.unistuttgart.ipvs.as.mmp.eam.repository.BusinessProcessRepository;
import de.unistuttgart.ipvs.as.mmp.eam.service.impl.BusinessProcessServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class BusinessProcessServiceTest {
    private static final String EXISTING_BP_LABEL = "test bp";
    private static final String NON_EXISTING_BP_LABEL = "non existing bp";
    private static final Long EXISTING_BP_ID = 1L;
    private static final Long NON_EXISTING_BP_ID = 12345L;

    @TestConfiguration
    static class BPServiceTestConfiguration {
        @MockBean
        public BusinessProcessRepository bpRepository;

        @Bean
        public BusinessProcessService employeeService() {
            return new BusinessProcessServiceImpl(bpRepository);
        }
    }

    private BusinessProcess testBP = new BusinessProcess();
    private BusinessProcess predecessorBP = new BusinessProcess();

    @Autowired
    BusinessProcessRepository bpRepository;

    @Autowired
    BusinessProcessService bpService;


    @BeforeEach
    public void setUp() {
        given(bpRepository.findByLabel(EXISTING_BP_LABEL)).willReturn(Optional.of(testBP));
        given(bpRepository.findByLabel(NON_EXISTING_BP_LABEL)).willReturn(Optional.empty());
        given(bpRepository.findById(EXISTING_BP_ID)).willReturn(Optional.of(testBP));
        given(bpRepository.findById(NON_EXISTING_BP_ID)).willReturn(Optional.empty());
        given(bpRepository.findAll()).willReturn(Arrays.asList(testBP));
    }

    @Test
    public void shouldGetAllBusinessProcesses() {
        List<BusinessProcess> allBusinessProcesses = bpService.getAllBusinessProcesses();
        assertEquals(Collections.singletonList(testBP), allBusinessProcesses);
    }

    @Test
    public void shouldGetBusinessProcessById() {
        Optional<BusinessProcess> optionalBP = bpService.getBusinessProcessById(EXISTING_BP_ID);
        assertEquals(Optional.of(testBP), optionalBP);
    }

    @Test
    public void shouldNotGetBusinessProcessById() {
        Optional<BusinessProcess> optionalBP = bpService.getBusinessProcessById(NON_EXISTING_BP_ID);
        assertEquals(Optional.empty(), optionalBP);
    }

    @Test
    public void shouldGetBusinessProcessByLabel() {
        Optional<BusinessProcess> optionalBP = bpService.getBusinessProcessByLabel(EXISTING_BP_LABEL);
        assertEquals(Optional.of(testBP), optionalBP);
    }

    @Test
    public void shouldNotGetBusinessProcessByLabel() {
        Optional<BusinessProcess> optionalBP = bpService.getBusinessProcessByLabel(NON_EXISTING_BP_LABEL);
        assertEquals(Optional.empty(), optionalBP);
    }

    @Test
    public void shouldUpdateBusinessProcessById() {
        BusinessProcess updatedBp = new BusinessProcess();
        updatedBp.setLabel("updated process");
        given(bpRepository.save(updatedBp)).willReturn(updatedBp);
        BusinessProcess businessProcess = bpService.updateBusinessProcessById(EXISTING_BP_ID, updatedBp);
        assertEquals(updatedBp, businessProcess);
    }

    @Test
    public void shouldNotUpdateBusinessProcessIdNotFound() {
        BusinessProcess updatedBp = new BusinessProcess();
        updatedBp.setLabel("updated process");
        given(bpRepository.save(updatedBp)).willReturn(updatedBp);
        assertThrows(IdException.class, () -> bpService.updateBusinessProcessById(NON_EXISTING_BP_ID, updatedBp));
    }

    @Test
    public void shouldNotUpdateBusinessProcessNull() {
        assertThrows(NullPointerException.class, () -> bpService.updateBusinessProcessById(EXISTING_BP_ID, null));
    }

    @Test
    public void shouldSaveBusinessProcesses() {
        BusinessProcess bp1 = BusinessProcess.builder().label("1").build();
        BusinessProcess bp1Exp = BusinessProcess.builder().label("1").build();
        bp1.setId(1L);
        bp1Exp.setId(1L);

        BusinessProcess bp2 = BusinessProcess.builder().label("2").build();
        BusinessProcess bp2Exp = BusinessProcess.builder().label("2").predecessor(bp1Exp).build();
        bp2.setId(2L);
        bp2Exp.setId(2L);

        BusinessProcess bp3 = BusinessProcess.builder().label("3").build();
        BusinessProcess bp3Exp = BusinessProcess.builder().label("3").predecessor(bp2Exp).build();
        bp3.setId(3L);
        bp3Exp.setId(3L);

        List<BusinessProcess> bpsExpected = Arrays.asList(bp1Exp, bp2Exp, bp3Exp);

        given(bpRepository.save(bp1Exp)).willReturn(bp1Exp);
        given(bpRepository.save(bp2Exp)).willReturn(bp2Exp);
        given(bpRepository.save(bp3Exp)).willReturn(bp3Exp);

        List<BusinessProcess> bps = bpService.saveBusinessProcesses(Arrays.asList(bp1, bp2, bp3));

        assertEquals(bpsExpected.get(0).getLabel(), bps.get(0).getLabel());
        assertEquals(bpsExpected.get(0).getPredecessor(), bps.get(0).getPredecessor());

        assertEquals(bpsExpected.get(1).getLabel(), bps.get(1).getLabel());
        assertEquals(bpsExpected.get(1).getPredecessor(), bps.get(1).getPredecessor());

        assertEquals(bpsExpected.get(2).getLabel(), bps.get(2).getLabel());
        assertEquals(bpsExpected.get(2).getPredecessor(), bps.get(2).getPredecessor());

    }

}