package de.unistuttgart.ipvs.as.mmp.eam.v1.controller;


import de.unistuttgart.ipvs.as.mmp.eam.domain.OrganisationUnit;
import de.unistuttgart.ipvs.as.mmp.eam.service.OrganisationUnitService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static de.unistuttgart.ipvs.as.mmp.eam.v1.controller.OrganisationUnitController.PATH;

@Controller
@CrossOrigin
@RequestMapping(value = PATH)
public class OrganisationUnitController {
    public static final String PATH = "/v1/organisationUnits";
    private final OrganisationUnitService orgaUnitService;
    private static final String ORGAUNIT_ID_PATTERN = "/{id}";

    public OrganisationUnitController(OrganisationUnitService orgaUnitService) {
        this.orgaUnitService = orgaUnitService;
    }

    @GetMapping
    public ResponseEntity<List<OrganisationUnit>> getAllOrgaUnits() {
        List<OrganisationUnit> allOrgaUnits = orgaUnitService.getAllOrgaUnits();
        return ResponseEntity.ok(allOrgaUnits);
    }

    @PostMapping
    public ResponseEntity<OrganisationUnit> saveOrgaUnit(@RequestBody OrganisationUnit organisationUnit) {
        OrganisationUnit savedOrgaUnit = orgaUnitService.saveOrgaUnit(organisationUnit);
        return ResponseEntity.created(URI.create(String.format("%s/%s", PATH, savedOrgaUnit.getId()))).body(savedOrgaUnit);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllOrgaUnits() {
        orgaUnitService.deleteAllOrgaUnits();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = ORGAUNIT_ID_PATTERN)
    public ResponseEntity<Void> deleteOrgaUnitById(@PathVariable Long id) {
        boolean deleted = orgaUnitService.deleteOrgaUnitById(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping(value = ORGAUNIT_ID_PATTERN)
    public ResponseEntity<OrganisationUnit> getOrgaUnitById(@PathVariable Long id) {
        Optional<OrganisationUnit> optionalOrgaUnit = orgaUnitService.getOrgaUnitById(id);
        return optionalOrgaUnit.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = ORGAUNIT_ID_PATTERN)
    public ResponseEntity<OrganisationUnit> updateOrganisationUnitById(@PathVariable Long id, @RequestBody OrganisationUnit organisationUnit) {
        OrganisationUnit updatedOrgaUnit = orgaUnitService.updateOrgaUnitById(id, organisationUnit);
        if(updatedOrgaUnit == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedOrgaUnit);
    }
}
