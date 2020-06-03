package de.unistuttgart.ipvs.as.mmp.eam.service.impl;

import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.eam.domain.OrganisationUnit;
import de.unistuttgart.ipvs.as.mmp.eam.repository.OrganisationUnitRepository;
import de.unistuttgart.ipvs.as.mmp.eam.service.OrganisationUnitService;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganisationUnitServiceImpl implements OrganisationUnitService {
    private final OrganisationUnitRepository organisationUnitRepository;

    public OrganisationUnitServiceImpl(OrganisationUnitRepository repository) {
        this.organisationUnitRepository = repository;
    }

    @Override
    public List<OrganisationUnit> getAllOrgaUnits() {
        return organisationUnitRepository.findAll();
    }

    @Override
    public Optional<OrganisationUnit> getOrgaUnitById(@NonNull Long id) {
        return organisationUnitRepository.findById(id);
    }

    @Override
    public OrganisationUnit saveOrgaUnit(OrganisationUnit organisationUnit) {
        return organisationUnitRepository.save(organisationUnit);
    }

    @Override
    public boolean deleteOrgaUnitById(Long id) {
        if (existsOrgaUnitById(id)) {
            organisationUnitRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public OrganisationUnit updateOrgaUnitById(Long id, OrganisationUnit organisationUnit) {
        Optional<OrganisationUnit> optionalOrgaUnit = organisationUnitRepository.findById(id);
        OrganisationUnit existingOrgaUnit = optionalOrgaUnit
                .orElseThrow(() -> IdException.idNotFound(OrganisationUnit.class, id));
        organisationUnit.setId(existingOrgaUnit.getId());
        return organisationUnitRepository.save(organisationUnit);
    }

    @Override
    public Optional<OrganisationUnit> getOrgaUnitByIndex(@NonNull Integer index) {
        return organisationUnitRepository.findByIndex(index);
    }

    @Override
    public boolean existsOrgaUnitById(Long id) {
        return organisationUnitRepository.existsById(id);
    }

    @Override
    public void deleteAllOrgaUnits() {
        organisationUnitRepository.deleteAll();
    }
}
