package de.unistuttgart.ipvs.as.mmp.eam.repository;

import de.unistuttgart.ipvs.as.mmp.eam.configuration.MmpJpaTestConfig;
import de.unistuttgart.ipvs.as.mmp.eam.domain.OrganisationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EntityScan(basePackages = "de.unistuttgart.ipvs.as.mmp")
@EnableJpaRepositories(basePackages = "de.unistuttgart.ipvs.as.mmp.eam.repository")
@Import(MmpJpaTestConfig.class)
public class OrganisationUnitRepoTest {

    @Autowired
    private OrganisationUnitRepository organisationUnitRepository;

    OrganisationUnit testOrgaUnit;

    @BeforeEach
    public void setUp() {
        organisationUnitRepository.deleteAll();
        testOrgaUnit = new OrganisationUnit("some orga unit", 0);
    }

    @Test
    public void shouldFindAllOrgaUnits() {
       organisationUnitRepository.save(testOrgaUnit);
        List<OrganisationUnit> organisationUnits = organisationUnitRepository.findAll();
        assertEquals(Collections.singletonList(testOrgaUnit), organisationUnits);
    }

    @Test
    public void shouldNotFindAnyOrgaUnit() {
        List<OrganisationUnit> all = organisationUnitRepository.findAll();
        assertEquals(Collections.emptyList(), all);
    }

    @Test
    public void shouldFindOrgaUnitById() {
        OrganisationUnit organisationUnit = organisationUnitRepository.save(testOrgaUnit);
        Long id = organisationUnit.getId();
        Optional<OrganisationUnit> optionalOrganisationUnit = organisationUnitRepository.findById(id);
        assertEquals(Optional.of(testOrgaUnit), optionalOrganisationUnit);
    }

    @Test
    public void shouldNotFindOrgaUnitById() {
        Optional<OrganisationUnit> optionalOrgaUnit = organisationUnitRepository.findById(1234L);
        assertEquals(Optional.empty(), optionalOrgaUnit);
    }

    @Test
    public void shouldFindOrgaUnitByIndex() {
        OrganisationUnit organisationUnit = organisationUnitRepository.save(testOrgaUnit);
        Integer index = organisationUnit.getIndex();
        Optional<OrganisationUnit> optionalOrgaUnit = organisationUnitRepository.findByIndex(index);
        assertEquals(Optional.of(testOrgaUnit), optionalOrgaUnit);
    }

    @Test
    public void shouldNotFindOrgaUnitByIndex() {
        Optional<OrganisationUnit> optionalOrgaUnit = organisationUnitRepository.findByIndex(99999);
        assertEquals(Optional.empty(), optionalOrgaUnit);
    }

}
