package de.unistuttgart.ipvs.as.mmp.eam.repository;

import de.unistuttgart.ipvs.as.mmp.eam.configuration.MmpJpaTestConfig;
import de.unistuttgart.ipvs.as.mmp.eam.domain.BusinessProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EntityScan(basePackages = "de.unistuttgart.ipvs.as.mmp")
@EnableJpaRepositories(basePackages = "de.unistuttgart.ipvs.as.mmp.eam.repository")
@Import(MmpJpaTestConfig.class)
public class BusinessProcessRepoTest {
    @Autowired
    BusinessProcessRepository bpRepository;

    private BusinessProcess testBP = new BusinessProcess();
    private BusinessProcess predecessorBP = new BusinessProcess();

    @BeforeEach
    public void setUp() {
        bpRepository.deleteAll();
        predecessorBP.setLabel("predecessor process");
        testBP.setLabel("test process");

    }

    @Test
    public void shouldFindAllBPs() {
        bpRepository.save(predecessorBP);
        testBP.setPredecessor(predecessorBP);
        bpRepository.save(testBP);
        List<BusinessProcess> businessProcessList = bpRepository.findAll();
        List<BusinessProcess> expected = Arrays.asList(predecessorBP, testBP);
        assertEquals(expected, businessProcessList);
    }

    @Test
    public void shouldNotFindAny() {
        List<BusinessProcess> businessProcesses = bpRepository.findAll();
        assertEquals(Collections.emptyList(), businessProcesses);
    }

    @Test
    public void shouldFindById() {
        BusinessProcess businessProcess = bpRepository.save(testBP);
        Long bpId = businessProcess.getId();
        Optional<BusinessProcess> optionalBP = bpRepository.findById(bpId);
        BusinessProcess bp = optionalBP.get();
        assertEquals(testBP, bp);
    }

    @Test
    public void shouldNotFindById() {
        Optional<BusinessProcess> optionalBP = bpRepository.findById(1233252L);
        assertEquals(Optional.empty(), optionalBP);
    }

    @Test
    public void shouldFindByLabel() {
        BusinessProcess savedBP = bpRepository.save(testBP);
        Optional<BusinessProcess> optionalBp = bpRepository.findByLabel(testBP.getLabel());
        BusinessProcess actualBP = optionalBp.get();
        assertEquals(testBP, actualBP);
    }

    @Test
    public void shouldNotFindByLabel() {
        Optional<BusinessProcess> optionalBP = bpRepository.findByLabel("some process");
        assertEquals(Optional.empty(), optionalBP);
    }

}
