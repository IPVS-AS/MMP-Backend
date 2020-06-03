package de.unistuttgart.ipvs.as.mmp.scoring.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.ScoringInput;

import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.ScoringOutput;
import de.unistuttgart.ipvs.as.mmp.scoring.service.ScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.unistuttgart.ipvs.as.mmp.scoring.v1.controller.ScoringController.PATH;

@Controller
@CrossOrigin
@RequestMapping(value = PATH)
public class ScoringController {

    public static final String PATH = "/v1/projects";
    private static final String PROJECT_ID_PATTERN = "/{projectId}";
    private static final String MODELS = "/models";
    private static final String MODEL_ID_PATTERN = "/{modelId}";
    private static final String SCORING = "/scoring";

    private final ScoringService scoringService;

    public ScoringController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @PostMapping(value = PROJECT_ID_PATTERN + MODELS + MODEL_ID_PATTERN + SCORING)
    public ResponseEntity<List<ScoringOutput>> scoreModel(@PathVariable Long projectId, @PathVariable Long modelId, @RequestBody List<ScoringInput> inputs) {
        List<ScoringOutput> outputs = scoringService.scoreModel(projectId, modelId, inputs);
        return ResponseEntity.ok(outputs);
    }
}
