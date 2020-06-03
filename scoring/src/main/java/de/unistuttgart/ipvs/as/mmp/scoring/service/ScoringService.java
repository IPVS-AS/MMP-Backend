package de.unistuttgart.ipvs.as.mmp.scoring.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.ScoringInput;
import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.ScoringOutput;

import java.util.List;

public interface ScoringService {

    List<ScoringOutput> scoreModel(Long projectId, Long modelId, List<ScoringInput> inputs);

}
