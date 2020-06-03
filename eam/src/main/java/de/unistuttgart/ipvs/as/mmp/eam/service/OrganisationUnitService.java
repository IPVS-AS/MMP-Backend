package de.unistuttgart.ipvs.as.mmp.eam.service;

import de.unistuttgart.ipvs.as.mmp.eam.domain.OrganisationUnit;

import java.util.List;
import java.util.Optional;

public interface OrganisationUnitService {

    List<OrganisationUnit> getAllOrgaUnits();

    Optional<OrganisationUnit> getOrgaUnitById(Long id);

    OrganisationUnit saveOrgaUnit(OrganisationUnit organisationUnit);

    boolean deleteOrgaUnitById(Long id);

    OrganisationUnit updateOrgaUnitById(Long id, OrganisationUnit organisationUnit);

    Optional<OrganisationUnit> getOrgaUnitByIndex(Integer index);

    boolean existsOrgaUnitById(Long id);

    void deleteAllOrgaUnits();
}
