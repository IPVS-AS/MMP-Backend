package de.unistuttgart.ipvs.as.mmp.eam.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.Project;
import de.unistuttgart.ipvs.as.mmp.common.domain.User;
import de.unistuttgart.ipvs.as.mmp.common.util.DefaultDataBuilder;
import de.unistuttgart.ipvs.as.mmp.eam.configuration.MmpJpaTestConfig;
import de.unistuttgart.ipvs.as.mmp.eam.domain.BusinessProcess;
import de.unistuttgart.ipvs.as.mmp.eam.domain.EAMContainer;
import de.unistuttgart.ipvs.as.mmp.eam.domain.OrganisationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EntityScan(basePackages = "de.unistuttgart.ipvs.as.mmp")
@EnableJpaRepositories(basePackages = "de.unistuttgart.ipvs.as.mmp.eam.repository")
@Import(MmpJpaTestConfig.class)
public class EAMContainerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EAMContainerRepository eamContainerRepository;

    private User testUser = new User("Hans Maier", "admin", "password");
    private Project testProject = Project.builder()
            .name("test project")
            .editors(Collections.singletonList(testUser))
            .build();
    private ModelMetadata testModelMetadata = DefaultDataBuilder.getNewMetadataBuilder().author(testUser).build();
    private ModelMetadata testModelMetadata2 = DefaultDataBuilder.getNewMetadataBuilder().author(testUser).build();
    private Model testModel = Model.builder().modelMetadata(testModelMetadata).project(testProject).build();
    private Model testModel2 = Model.builder().modelMetadata(testModelMetadata2).project(testProject).build();
    private BusinessProcess testBusinessProcess = BusinessProcess.builder().label("testBusinessProcess").build();
    private OrganisationUnit testOrgUnit = OrganisationUnit.builder().label("testOrgaUnit").build();
    private EAMContainer testEamContainer = EAMContainer.builder()
            .businessProcess(testBusinessProcess)
            .organisationUnit(testOrgUnit)
            .model(testModel).build();
    private EAMContainer testEamContainer2 = EAMContainer.builder()
            .businessProcess(testBusinessProcess)
            .organisationUnit(testOrgUnit)
            .model(testModel).build();
    private EAMContainer testEamContainer3 = EAMContainer.builder()
            .businessProcess(testBusinessProcess)
            .organisationUnit(testOrgUnit)
            .model(testModel2).build();

    @BeforeEach
    public void setUp() {
        eamContainerRepository.deleteAll();
        entityManager.persist(testUser);
        entityManager.persist(testProject);
        entityManager.persist(testModel);
        entityManager.persist(testModel2);
        entityManager.persist(testBusinessProcess);
        entityManager.persist(testOrgUnit);
    }

    @Test
    public void shouldFindAll() {
        entityManager.persist(testEamContainer);

        List<EAMContainer> eamContainers = eamContainerRepository.findAll();
        assertEquals(Collections.singletonList(testEamContainer), eamContainers);
    }

    @Test
    public void shouldNotFindAny() {
        List<EAMContainer> eamContainers = eamContainerRepository.findAll();
        assertEquals(Collections.emptyList(), eamContainers);
    }

    @Test
    public void shouldNotFindById() {
        Optional<EAMContainer> eamContainer = eamContainerRepository.findById(123456789L);
        assertEquals(Optional.empty(), eamContainer);
    }

    @Test
    public void shouldFindById() {
        Long eamContainerId = entityManager.persistAndGetId(testEamContainer, Long.class);

        Optional<EAMContainer> eamContainer = eamContainerRepository.findById(eamContainerId);
        assertEquals(Optional.of(testEamContainer), eamContainer);
    }

    @Test
    public void shouldUpdate() {
        eamContainerRepository.save(testEamContainer);
        BusinessProcess businessProcess = BusinessProcess.builder().label("test").build();
        entityManager.persist(businessProcess);
        EAMContainer updatedEamContainer = EAMContainer.builder()
                .businessProcess(businessProcess)
                .organisationUnit(testOrgUnit)
                .model(testModel).build();
        updatedEamContainer.setId(testEamContainer.getId());
        eamContainerRepository.save(updatedEamContainer);
        assertEquals(updatedEamContainer, eamContainerRepository.findById(testEamContainer.getId()).get());
    }

    @Test
    public void shouldFindAllEAMContainersByModelId() {
        entityManager.persist(testEamContainer);
        entityManager.persist(testEamContainer2);
        entityManager.persist(testEamContainer3);

        List<EAMContainer> actualEAMContainers1 = eamContainerRepository.findAllByModel_Id(testModel.getId());
        List<EAMContainer> expectedEAMContainers1 = Arrays.asList(testEamContainer, testEamContainer2);

        List<EAMContainer> actualEAMContainers2 = eamContainerRepository.findAllByModel_Id(testModel2.getId());
        List<EAMContainer> expectedEAMContainers2 = Collections.singletonList(testEamContainer3);

        assertIterableEquals(expectedEAMContainers1, actualEAMContainers1);
        assertIterableEquals(expectedEAMContainers2, actualEAMContainers2);
    }

    @Test
    public void shouldFindNoEAMContainerByModelId() {
        Model testModel4 = Model.builder().project(testProject).build();
        Long testModel4Id = entityManager.persistAndGetId(testModel4, Long.class);

        List<EAMContainer> eamContainer = eamContainerRepository.findAllByModel_Id(testModel4Id);

        assertEquals(0, eamContainer.size());
    }

}
