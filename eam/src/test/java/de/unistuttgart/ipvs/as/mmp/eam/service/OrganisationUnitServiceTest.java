package de.unistuttgart.ipvs.as.mmp.eam.service;

import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.eam.domain.OrganisationUnit;
import de.unistuttgart.ipvs.as.mmp.eam.repository.OrganisationUnitRepository;
import de.unistuttgart.ipvs.as.mmp.eam.service.impl.OrganisationUnitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class OrganisationUnitServiceTest {

    private static final Integer EXISTING_ORGA_UNIT_INDEX = 0;
    private static final Integer NON_EXISTING_ORGA_UNIT_INDEX = 999;
    private static final Long EXISTING_ORGA_UNIT_ID = 1245L;
    private static final Long NON_EXISTING_ORGA_UNIT_ID = 1243425L;
    private OrganisationUnit testOrgaUnit = new OrganisationUnit("test orga unit", 0);

    @TestConfiguration
    static class ProjectServiceTestContextConfiguration {

        @MockBean
        public OrganisationUnitRepository organisationUnitRepository;

        @Bean
        public OrganisationUnitService employeeService() {
            return new OrganisationUnitServiceImpl(this.organisationUnitRepository);
        }
    }

    @Autowired
    OrganisationUnitService organisationUnitService;

    @Autowired
    OrganisationUnitRepository organisationUnitRepository;

    @BeforeEach
    public void setUp() {
        given(organisationUnitRepository.findByIndex(EXISTING_ORGA_UNIT_INDEX)).willReturn(Optional.of(testOrgaUnit));
        given(organisationUnitRepository.findByIndex(NON_EXISTING_ORGA_UNIT_INDEX)).willReturn(Optional.empty());
        given(organisationUnitRepository.findById(EXISTING_ORGA_UNIT_ID)).willReturn(Optional.of(testOrgaUnit));
        given(organisationUnitRepository.findById(NON_EXISTING_ORGA_UNIT_ID)).willReturn(Optional.empty());
        given(organisationUnitRepository.findAll()).willReturn(Arrays.asList(testOrgaUnit));
    }

    @Test
    public void shouldGetAllOrgaUnits() {
        List<OrganisationUnit> allOrgaUnits = organisationUnitService.getAllOrgaUnits();
        assertEquals(Arrays.asList(testOrgaUnit), allOrgaUnits);
    }

    @Test
    public void shouldGetOrgaUnitById() {
        Optional<OrganisationUnit> optionalOrganisationUnit = organisationUnitService.getOrgaUnitById(EXISTING_ORGA_UNIT_ID);
        assertEquals(Optional.of(testOrgaUnit), optionalOrganisationUnit);
    }

    @Test
    public void shouldNotGetOrgaUnitById() {
        Optional<OrganisationUnit> optionalOrganisationUnit = organisationUnitService.getOrgaUnitById(NON_EXISTING_ORGA_UNIT_ID);
        assertEquals(Optional.empty(), optionalOrganisationUnit);
    }

    @Test
    public void shouldGetOrgaUnitByIndex() {
        Optional<OrganisationUnit> optionalOrganisationUnit = organisationUnitService.getOrgaUnitByIndex(EXISTING_ORGA_UNIT_INDEX);
        assertEquals(Optional.of(testOrgaUnit), optionalOrganisationUnit);
    }

    @Test
    public void shouldNotGetOrgaUnitByIndex() {
        Optional<OrganisationUnit> optionalOrganisationUnit = organisationUnitService.getOrgaUnitByIndex(NON_EXISTING_ORGA_UNIT_INDEX);
        assertEquals(Optional.empty(), optionalOrganisationUnit);
    }

    @Test
    public void shouldUpdateOrgaUnitById() {
        OrganisationUnit updatedOrgaUnit = new OrganisationUnit("updated Orga unit", 1);
        given(organisationUnitRepository.save(updatedOrgaUnit)).willReturn(updatedOrgaUnit);
        OrganisationUnit organisationUnit = organisationUnitService.updateOrgaUnitById(EXISTING_ORGA_UNIT_ID, updatedOrgaUnit);
        assertEquals(updatedOrgaUnit, organisationUnit);
    }

    @Test
    public void shouldNotUpdateOrgaUnitIdNotFound() {
        OrganisationUnit updatedOrgaUnit = new OrganisationUnit("updated Orga unit", 1);
        given(organisationUnitRepository.save(updatedOrgaUnit)).willReturn(updatedOrgaUnit);
        assertThrows(IdException.class, () -> organisationUnitService.updateOrgaUnitById(NON_EXISTING_ORGA_UNIT_ID, updatedOrgaUnit));
    }

    @Test
    public void shouldNotUpdateOrgaUnitNull() {
        assertThrows(NullPointerException.class, () -> organisationUnitService.updateOrgaUnitById(EXISTING_ORGA_UNIT_ID, null));
    }
}
