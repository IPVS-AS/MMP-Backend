package de.unistuttgart.ipvs.as.mmp.model.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelGroup;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static de.unistuttgart.ipvs.as.mmp.model.v1.controller.ModelController.PATH;

@Controller
@CrossOrigin
@RequestMapping(value = PATH)
public class ModelController {

    public static final String PATH = "/v1";
    private static final String PROJECT_PATTERN = "/projects";
    private static final String PROJECT_ID_PATTERN = "/{projectId}";
    private static final String MODELS = "/models";
    private static final String ALL_MODEL_GROUPS_PATTERN = "/modelGroups";
    private static final String MODEL_GROUP_PATTERN = "/{modelGroupId}";
    private static final String MODEL_ID_PATTERN = "/{modelId}";
    private static final String LATEST = "/latest";
    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping(value = MODELS)
    public ResponseEntity<List<Model>> getAllModels() {
        List<Model> models = modelService.getAllModels();
        return ResponseEntity.ok(models);
    }

    @GetMapping(value = PROJECT_PATTERN + PROJECT_ID_PATTERN + MODELS)
    public ResponseEntity<List<Model>> getAllModelsForProject(@PathVariable Long projectId) {
        List<Model> models = modelService.getAllModelsForProject(projectId);
        return ResponseEntity.ok(models);
    }

    @PostMapping(value = PROJECT_PATTERN + PROJECT_ID_PATTERN + MODELS)
    public ResponseEntity<Model> saveModelForProjectById(@PathVariable Long projectId, @RequestBody Model model) {
        Model savedModel = modelService.saveModelForProject(projectId, model);
        if (savedModel == null || savedModel.getProject() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.created(URI.create(String.format("%s%s/%s%s/%s", PATH, PROJECT_PATTERN,
                savedModel.getProject().getId(), MODELS, savedModel.getId()))).body(model);
    }

    @DeleteMapping(value = PROJECT_PATTERN + PROJECT_ID_PATTERN + MODELS)
    public ResponseEntity<Void> deleteAllModelsForProject(@PathVariable Long projectId) {
        modelService.deleteAllModelsForProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = PROJECT_PATTERN + PROJECT_ID_PATTERN + MODELS + MODEL_ID_PATTERN)
    public ResponseEntity<Model> getModelForProjectById(@PathVariable Long projectId, @PathVariable Long modelId) {
        Optional<Model> modelOptional = modelService.getModelForProjectById(projectId, modelId);
        return modelOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = PROJECT_PATTERN + PROJECT_ID_PATTERN + MODELS + MODEL_ID_PATTERN)
    public ResponseEntity<Model> updateModelForProjectById(@PathVariable Long projectId, @PathVariable Long modelId, @RequestBody Model model) {
        Model updatedModel = modelService.updateModelForProjetById(projectId, modelId, model);
        if (updatedModel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(updatedModel);
    }

    @DeleteMapping(value = PROJECT_PATTERN + PROJECT_ID_PATTERN + MODELS + MODEL_ID_PATTERN)
    public ResponseEntity<Void> deleteModelForProjectById(@PathVariable Long projectId, @PathVariable Long modelId) {
        if (modelService.deleteModelForProjectById(projectId, modelId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = PROJECT_PATTERN + PROJECT_ID_PATTERN + MODELS + ALL_MODEL_GROUPS_PATTERN)
    public ResponseEntity<List<ModelGroup>> getModelGroupIdentifiersByProject(@PathVariable Long projectId) {
        List<ModelGroup> modelGroups = modelService.getAllModelGroupsForProject(projectId);
        if (modelGroups == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(modelGroups);
    }

    @GetMapping(value = PROJECT_PATTERN + PROJECT_ID_PATTERN + MODELS + ALL_MODEL_GROUPS_PATTERN + MODEL_GROUP_PATTERN + LATEST)
    public ResponseEntity<Model> getLastModelForProjectAndModelGroupIdentifier(@PathVariable Long projectId, @PathVariable Long modelGroupId) {
        Optional<Model> modelOptional = modelService.getLastModelForProjectAndModelGroupIdentifier(projectId, modelGroupId);
        return modelOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = PROJECT_PATTERN + PROJECT_ID_PATTERN + MODELS + ALL_MODEL_GROUPS_PATTERN + MODEL_GROUP_PATTERN)
    public ResponseEntity<List<Model>> getModelsForProjectAndModelGroupIdentifier(@PathVariable Long projectId, @PathVariable Long modelGroupId) {
        List<Model> models = modelService.getModelsForProjectAndModelGroupIdentifier(projectId, modelGroupId);
        if (CollectionUtils.isEmpty(models)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(models);
    }
}