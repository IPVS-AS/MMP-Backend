package de.unistuttgart.ipvs.as.mmp.eam.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.util.DefaultDataBuilder;
import de.unistuttgart.ipvs.as.mmp.eam.domain.BusinessProcess;
import de.unistuttgart.ipvs.as.mmp.eam.domain.EAMContainer;
import de.unistuttgart.ipvs.as.mmp.eam.domain.OrganisationUnit;
import de.unistuttgart.ipvs.as.mmp.eam.repository.EAMContainerRepository;
import de.unistuttgart.ipvs.as.mmp.eam.service.impl.EAMContainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class EAMContainerServiceTest {

    private static final Long EXISTING_EAM_CONTAINER_ID = 1L;
    private static final Long NON_EXISTING_EAM_CONTAINER_ID = 12345L;
    private static final Long MODEL_ID = 123L;

    @TestConfiguration
    static class EAMContainerServiceTestConfiguration {
        @MockBean
        public EAMContainerRepository eamContainerRepository;

        @Bean
        public EAMContainerService eamContainerService() {
            return new EAMContainerServiceImpl(this.eamContainerRepository);
        }
    }

    @Autowired
    private EAMContainerRepository eamContainerRepository;

    @Autowired
    private EAMContainerService eamContainerService;

    private ModelMetadata testModelMetadata = DefaultDataBuilder.getNewMetadataBuilder().build();
    private Model testModel = Model.builder().modelMetadata(testModelMetadata).build();
    private BusinessProcess testBusinessProcess = BusinessProcess.builder().label("testBusinessProcess").build();
    private OrganisationUnit testOrgUnit = OrganisationUnit.builder().label("testOrgaUnit").index(0).build();
    private EAMContainer testEamContainer = EAMContainer.builder()
            .businessProcess(testBusinessProcess)
            .organisationUnit(testOrgUnit)
            .model(testModel).build();

    @BeforeEach
    public void setUp() {
        testModel.setId(MODEL_ID);
        given(eamContainerRepository.findAll()).willReturn(Collections.singletonList(testEamContainer));
        given(eamContainerRepository.findById(EXISTING_EAM_CONTAINER_ID)).willReturn(Optional.of(testEamContainer));
        given(eamContainerRepository.findById(NON_EXISTING_EAM_CONTAINER_ID)).willReturn(Optional.empty());
        given(eamContainerRepository.save(testEamContainer)).willReturn(testEamContainer);
    }

    @Test
    public void shouldGetAllEAMContainers() {
        List<EAMContainer> expectedEamContainers = eamContainerService.getAllEAMContainers();
        assertEquals(Collections.singletonList(testEamContainer), expectedEamContainers);
    }

    @Test
    public void shouldGetAllEAMContainersByModelId() {
        given(eamContainerRepository.findAllByModel_Id(testModel.getId())).willReturn(Collections.singletonList(testEamContainer));
        List<EAMContainer> eamContainer = eamContainerService.getAllEAMContainersByModelId(testModel.getId());
        assertEquals(testEamContainer.getId(), eamContainer.get(0).getId());
    }

    @Test
    public void shouldGetNoEAMContainerByModelId() {
        given(eamContainerRepository.findAllByModel_Id(testModel.getId())).willReturn(Collections.emptyList());
        List<EAMContainer> eamContainer = eamContainerService.getAllEAMContainersByModelId(testModel.getId());
        assertEquals(0, eamContainer.size());
    }

    @Test
    public void shouldBeTrueIsModelContainedInEAMContainer() {
        given(eamContainerRepository.findAllByModel_Id(testModel.getId())).willReturn(Collections.singletonList(testEamContainer));
        assertEquals(true, eamContainerService.isModelContainedInEAMContainer(testModel.getId()));
    }

    @Test
    public void shouldBeFalseIsModelContainedInEAMContainer() {
        given(eamContainerRepository.findAllByModel_Id(testModel.getId())).willReturn(Collections.emptyList());
        assertEquals(false, eamContainerService.isModelContainedInEAMContainer(testModel.getId()));
    }

    @Test
    public void shouldUpdateEAMContainer() {
        EAMContainer expectedEamContainer = eamContainerService.updateEAMContainerById(EXISTING_EAM_CONTAINER_ID, testEamContainer);
        assertEquals(testEamContainer, expectedEamContainer);
    }

    @Test
    public void shouldNotUpdateEAMContainer() {
        assertThrows(IdException.class,
                () -> eamContainerService.updateEAMContainerById(NON_EXISTING_EAM_CONTAINER_ID, testEamContainer));
    }

    @Test
    public void shouldSaveEAMContainer() {
        EAMContainer expectedEamContainer = eamContainerService.saveEAMContainer(testEamContainer);
        assertEquals(testEamContainer, expectedEamContainer);
    }

    @Test
    public void shouldSaveEAMContainers() {
        given(eamContainerRepository.saveAll(Collections.singletonList(testEamContainer))).willReturn(Collections.singletonList(testEamContainer));
        List<EAMContainer> expectedEamContainers = eamContainerService.saveEAMContainers(Collections.singletonList(testEamContainer));
        assertEquals(testEamContainer, expectedEamContainers.get(0));
    }

    @Test
    public void shouldDeleteEAMContainer() {
        assertTrue(eamContainerService.deleteEAMContainer(EXISTING_EAM_CONTAINER_ID));
    }

    @Test
    public void shouldNotDeleteEAMContainer() {
        assertFalse(eamContainerService.deleteEAMContainer(NON_EXISTING_EAM_CONTAINER_ID));
    }

    @Test
    public void shouldAllDeleteEAMContainer() {
        eamContainerService.deleteAllEAMContainer();
        verify(eamContainerRepository, times(1)).deleteAll();
    }

}
