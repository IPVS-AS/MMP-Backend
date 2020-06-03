package de.unistuttgart.ipvs.as.mmp.model.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import de.unistuttgart.ipvs.as.mmp.model.service.OpcuaInformationModelService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static de.unistuttgart.ipvs.as.mmp.model.v1.controller.OpcuaInformationModelController.PATH;

@Controller
@CrossOrigin
@RequestMapping(value = PATH)
public class OpcuaInformationModelController {

    private static final String PROJECTS_PATTERN = "/v1/projects";
    private static final String PROJECT_ID_PATTERN = "/{projectId}";
    private static final String MODELS_PATTERN = "/models";
    public static final String PATH = PROJECTS_PATTERN + PROJECT_ID_PATTERN + MODELS_PATTERN;

    private static final String MODEL_ID_PATTERN = "/{modelId}";
    private static final String OPCUA_INFORMATION_MODEL_PATTERN = "/opcuainformationmodel";
    private static final String OPCUA_INFORMATION_MODEL_ID_PATTERN = "/{opcuaInformationModelId}";
    private static final String OPCUA_INFORMATION_MODEL_PARSE_PATTERN = "/parse";
    private static final String OPCUA_INFORMATION_MODEL_RAW_PATTERN = "/raw";

    private final OpcuaInformationModelService opcuaInformationModelService;

    public OpcuaInformationModelController(OpcuaInformationModelService opcuaInformationModelService) {
        this.opcuaInformationModelService = opcuaInformationModelService;
    }

    @GetMapping(MODEL_ID_PATTERN + OPCUA_INFORMATION_MODEL_PATTERN + OPCUA_INFORMATION_MODEL_ID_PATTERN)
    public ResponseEntity<OpcuaInformationModel> getOpcuaInformationModel(@PathVariable Long opcuaInformationModelId) {
        Optional<OpcuaInformationModel> opcuaInformationModel = opcuaInformationModelService
                .getOpcuaInformationModelById(opcuaInformationModelId);
        return opcuaInformationModel.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(MODEL_ID_PATTERN + OPCUA_INFORMATION_MODEL_PATTERN + OPCUA_INFORMATION_MODEL_ID_PATTERN +
            OPCUA_INFORMATION_MODEL_RAW_PATTERN)
    public ResponseEntity<Resource> getOpcuaInformationModelRaw(@PathVariable Long opcuaInformationModelId) {
        Optional<DBFile> dbFileOptional =
                opcuaInformationModelService.getOpcuaInformationModelRaw(opcuaInformationModelId);

        if (dbFileOptional.isPresent()) {
            DBFile dbFile = dbFileOptional.get();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(dbFile.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getFileName() + "\"")
                    .body(new ByteArrayResource(dbFile.getData()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(MODEL_ID_PATTERN + OPCUA_INFORMATION_MODEL_PATTERN)
    public ResponseEntity<OpcuaInformationModel> saveOpucaInformationModel(@PathVariable Long projectId,
                                                                           @PathVariable Long modelId,
                                                                           @RequestPart(value = "machineTypeId", required = false) String machineTypeId,
                                                                           @RequestPart(value = "sensorTypeId", required = false) String sensorTypeId,
                                                                           @RequestParam("file") MultipartFile file) {

        OpcuaInformationModel opcuaInformationModel =
                opcuaInformationModelService.saveOpcuaInformationModel(file, projectId, modelId, machineTypeId, sensorTypeId);

        if (opcuaInformationModel == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not parse OPC UA information model.");
        }

        return ResponseEntity.created(URI.create(String.format("%s/%s%s/%s%s/%s",
                PROJECTS_PATTERN, projectId, MODELS_PATTERN, modelId, OPCUA_INFORMATION_MODEL_PATTERN,
                opcuaInformationModel.getId()))).build();
    }

    @GetMapping(MODEL_ID_PATTERN + OPCUA_INFORMATION_MODEL_PATTERN)
    public ResponseEntity<List<OpcuaInformationModel>> getAllOpcuaModels(@PathVariable Long modelId, @PathVariable Long projectId) {
        List<OpcuaInformationModel> opcuaInformationModels =
                opcuaInformationModelService.getAllOpcuaInformationModelsByModelAndProjectId(projectId, modelId);
        return ResponseEntity.ok(opcuaInformationModels);
    }

    @DeleteMapping(MODEL_ID_PATTERN + OPCUA_INFORMATION_MODEL_PATTERN)
    public ResponseEntity<Void> deleteAllOpcuaModels(@PathVariable Long modelId, @PathVariable Long projectId) {
        boolean deletedAll = opcuaInformationModelService.deleteAllOpcuaInformationModelsForModel(projectId, modelId);
        return deletedAll ?
                ResponseEntity.noContent().build() :
                ResponseEntity.notFound().build();
    }

    @DeleteMapping(MODEL_ID_PATTERN + OPCUA_INFORMATION_MODEL_PATTERN + OPCUA_INFORMATION_MODEL_ID_PATTERN)
    public ResponseEntity<OpcuaInformationModel> deleteModelFile(@PathVariable Long modelId,
                                                                 @PathVariable Long projectId,
                                                                 @PathVariable Long opcuaInformationModelId) {

        if (opcuaInformationModelService.deleteOpcuaInformationModel(opcuaInformationModelId, projectId, modelId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(OPCUA_INFORMATION_MODEL_PATTERN + OPCUA_INFORMATION_MODEL_PARSE_PATTERN)
    public ResponseEntity<OpcuaInformationModel> parseOpcuaInformationModel(
            @RequestPart(value = "machineTypeId", required = false) String machineTypeId,
            @RequestPart(value = "sensorTypeId", required = false) String sensorTypeId,
            @RequestParam("file") MultipartFile file) {


        OpcuaInformationModel opcuaInformationModel =
                opcuaInformationModelService.parseOpcuaInformationModelRaw(file, machineTypeId, sensorTypeId);
        return ResponseEntity.ok(opcuaInformationModel);
    }
}
