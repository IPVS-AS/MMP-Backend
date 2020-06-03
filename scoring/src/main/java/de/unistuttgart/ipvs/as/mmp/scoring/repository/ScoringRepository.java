package de.unistuttgart.ipvs.as.mmp.scoring.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.Scoring;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoringRepository extends JpaRepository<Scoring, Long> {

}
