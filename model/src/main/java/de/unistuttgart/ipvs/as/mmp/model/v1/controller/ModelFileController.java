package de.unistuttgart.ipvs.as.mmp.model.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelFile;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelFileService;
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

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static de.unistuttgart.ipvs.as.mmp.model.v1.controller.ModelFileController.PATH;

@Controller
@CrossOrigin
@RequestMapping(value = PATH)
public class ModelFileController {

    private static final String PROJECTS_PATTERN = "/v1/projects";
    private static final String PROJECT_ID_PATTERN = "/{projectId}";
    private static final String MODELS_PATTERN = "/models";
    public static final String PATH = PROJECTS_PATTERN + PROJECT_ID_PATTERN + MODELS_PATTERN;

    private static final String MODEL_ID_PATTERN = "/{modelId}";
    private static final String MODEL_FILE_PATTERN = "/file";
    private static final String MODEL_FILE_ID_PATTERN = "/{modelFileId}";
    private static final String MODEL_FILE_PARSE_PATTERN = "/parse";
    private static final String MODEL_FILE_RAW_PATTERN = "/raw";
    public static final String RDS_FILE_ENDING = ".rds";
    public static final String XML_FILE_ENDING = ".xml";
    public static final String PMML_FILE_ENDING = ".pmml";
    private static final String PICKLE_FILE_ENDING = ".pkl";

    private final ModelFileService modelFileService;
    private final ObjectMapper objectMapper;

    public ModelFileController(ModelFileService modelFileService, ObjectMapper objectMapper) {
        this.modelFileService = modelFileService;
        this.objectMapper = objectMapper;
    }

    @GetMapping(MODEL_ID_PATTERN + MODEL_FILE_PATTERN + MODEL_FILE_ID_PATTERN)
    public ResponseEntity<ModelFile> getModelFile(@PathVariable Long modelFileId) {
        Optional<ModelFile> modelFile = modelFileService.getModelFileById(modelFileId);
        return modelFile.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(MODEL_ID_PATTERN + MODEL_FILE_PATTERN + MODEL_FILE_ID_PATTERN + MODEL_FILE_RAW_PATTERN)
    public ResponseEntity<Resource> getModelFileRaw(@PathVariable Long modelFileId) {
        Optional<DBFile> dbFileOptional = modelFileService.getModelFileRaw(modelFileId);

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

    @PostMapping(MODEL_ID_PATTERN + MODEL_FILE_PATTERN)
    public ResponseEntity<ModelFile> saveModelFile(@PathVariable Long modelId,
                                                   @PathVariable Long projectId, @RequestParam("file") MultipartFile file) {
        ModelFile modelFile =
                modelFileService.saveModelFile(file, modelId, projectId);

        if (modelFile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not save PMML File.");
        }
        return ResponseEntity.created(URI.create(String.format("%s/%s%s/%s%s/%s",
                PROJECTS_PATTERN, projectId, MODELS_PATTERN, modelId, MODEL_FILE_PATTERN, modelFile.getId()))).build();
    }

    @DeleteMapping(MODEL_ID_PATTERN + MODEL_FILE_PATTERN + MODEL_FILE_ID_PATTERN)
    public ResponseEntity<ModelFile> deleteModelFile(@PathVariable Long modelId,
                                                     @PathVariable Long projectId, @PathVariable Long modelFileId) {
        if (modelFileService.deleteModelFile(modelFileId, modelId, projectId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(MODEL_FILE_PATTERN + MODEL_FILE_PARSE_PATTERN)
    public ResponseEntity<Model> parsePMMLModelFileInModel(@RequestPart("model") String model,
                                                           @RequestParam("file") MultipartFile file) {
        Model resultModel;
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename.endsWith(XML_FILE_ENDING) || originalFilename.endsWith(PMML_FILE_ENDING)) {
                resultModel = modelFileService.parsePMMLModelFileInModel(file, objectMapper.readValue(model, Model.class));
            } else if (originalFilename.endsWith(RDS_FILE_ENDING)) {
                resultModel = modelFileService.parseRModelFileInModel(file, objectMapper.readValue(model, Model.class));
            }else if(originalFilename.endsWith(PICKLE_FILE_ENDING)) {
                resultModel = modelFileService.parsePickleFileInModel(file, objectMapper.readValue(model, Model.class));
            }else {
                throw new IllegalArgumentException("File type is currently not supported!");
            }
            return ResponseEntity.ok(resultModel);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
