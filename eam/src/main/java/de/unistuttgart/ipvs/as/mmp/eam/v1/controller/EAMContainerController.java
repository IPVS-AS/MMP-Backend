package de.unistuttgart.ipvs.as.mmp.eam.v1.controller;

import de.unistuttgart.ipvs.as.mmp.eam.domain.EAMContainer;
import de.unistuttgart.ipvs.as.mmp.eam.service.EAMContainerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static de.unistuttgart.ipvs.as.mmp.eam.v1.controller.EAMContainerController.PATH;

@Controller
@CrossOrigin
@RequestMapping(value = PATH)
public class EAMContainerController {

    public static final String PATH = "/v1/eam";
    private static final String PATH_ALL_EAM_CONTAINER = "/all";
    private static final String EAM_BY_MODEL_ID_PATTERN = "/models/{modelId}";
    private static final String EAM_CONTAINER_ID_PATTERN = "/{eamContainerId}";

    private final EAMContainerService eamContainerService;

    public EAMContainerController(EAMContainerService eamContainerService) {
        this.eamContainerService = eamContainerService;
    }

    @GetMapping
    public ResponseEntity<List<EAMContainer>> getAllEAMContainers() {
        List<EAMContainer> eamContainers = eamContainerService.getAllEAMContainers();
        return ResponseEntity.ok(eamContainers);
    }

    @GetMapping(value = EAM_BY_MODEL_ID_PATTERN)
    public ResponseEntity<List<EAMContainer>> getAllEAMContainerByModelId(@PathVariable Long modelId) {
        List<EAMContainer> eamContainer = eamContainerService.getAllEAMContainersByModelId(modelId);
        if (eamContainer == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(eamContainer);
    }

    @PutMapping(value = EAM_CONTAINER_ID_PATTERN)
    public ResponseEntity<EAMContainer> updateEAMContainerById(@PathVariable Long eamContainerId, @RequestBody EAMContainer eamContainer) {
        EAMContainer updatedEamContainer = eamContainerService.updateEAMContainerById(eamContainerId, eamContainer);
        if (updatedEamContainer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedEamContainer);
    }

    @PostMapping
    public ResponseEntity<EAMContainer> saveEAMContainer(@RequestBody EAMContainer eamContainer) {
        EAMContainer savedEamContainer = eamContainerService.saveEAMContainer(eamContainer);
        return ResponseEntity.created(URI.create(String.format("%s/%s", PATH, savedEamContainer.getId())))
                .body(savedEamContainer);
    }

    @PostMapping(value = PATH_ALL_EAM_CONTAINER)
    public ResponseEntity<List<EAMContainer>> saveEAMContainers(@RequestBody List<EAMContainer> eamContainers) {
        List<EAMContainer> savedEamContainers = eamContainerService.saveEAMContainers(eamContainers);
        return ResponseEntity.created(URI.create(PATH)).body(savedEamContainers);
    }

    @DeleteMapping(value = EAM_CONTAINER_ID_PATTERN)
    public ResponseEntity<Void> deleteEAMContainerById(@PathVariable Long eamContainerId) {
        if (eamContainerService.deleteEAMContainer(eamContainerId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllEAMContainer() {
        eamContainerService.deleteAllEAMContainer();
        return ResponseEntity.noContent().build();
    }
}
