package de.unistuttgart.ipvs.as.mmp.eam.v1.controller;

import de.unistuttgart.ipvs.as.mmp.eam.domain.BusinessProcess;
import de.unistuttgart.ipvs.as.mmp.eam.service.BusinessProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static de.unistuttgart.ipvs.as.mmp.eam.v1.controller.BusinessProcessController.PATH;

@Controller
@CrossOrigin
@RequestMapping(value = PATH)
public class BusinessProcessController {

    private BusinessProcessService bpService;
    public static final String PATH = "/v1/businessprocesses";
    public static final String BP_ID_PATTERN = "/{bpId}";

    public BusinessProcessController(BusinessProcessService bpService) {
        this.bpService = bpService;
    }

    @GetMapping
    public ResponseEntity<List<BusinessProcess>> getAllBusinessProcesses() {
        List<BusinessProcess> allBusinessProcesses = bpService.getAllBusinessProcesses();
        return ResponseEntity.ok(allBusinessProcesses);
    }

    @PostMapping
    public ResponseEntity<List<BusinessProcess>> saveBusinessProcesses(@RequestBody List<BusinessProcess> businessProcesses) {
        List<BusinessProcess> savedBusinessProcesses = bpService.saveBusinessProcesses(businessProcesses);

        if (savedBusinessProcesses == null || savedBusinessProcesses.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        URI uri;
        if (savedBusinessProcesses.size() == 1) {
            uri = URI.create(String.format("%s/%s", PATH, savedBusinessProcesses.get(0).getId()));
        } else {
            uri = URI.create(String.format("%s", PATH));
        }

        return ResponseEntity.created(uri).body(savedBusinessProcesses);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllBusinessProcesses() {
        bpService.deleteAllBusinessProcesses();
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = BP_ID_PATTERN)
    public ResponseEntity<BusinessProcess> getBusinessProcessById(@PathVariable Long bpId) {
        Optional<BusinessProcess> optionalBusinessProcess = bpService.getBusinessProcessById(bpId);
        return optionalBusinessProcess.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = BP_ID_PATTERN)
    public ResponseEntity<BusinessProcess> updateBusinessProcessById(@PathVariable Long bpId, @RequestBody BusinessProcess businessProcess) {
        BusinessProcess updatedBusinessProcess = bpService.updateBusinessProcessById(bpId, businessProcess);
        if (updatedBusinessProcess == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedBusinessProcess);
    }

    @DeleteMapping(value = BP_ID_PATTERN)
    public ResponseEntity<Void> deleteBusinessProcessById(@PathVariable Long bpId) {
        boolean deleted = bpService.deleteBusinessProcessById(bpId);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
