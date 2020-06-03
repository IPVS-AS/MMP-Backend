package de.unistuttgart.ipvs.as.mmp.scoring.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.Project;
import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.Scoring;
import de.unistuttgart.ipvs.as.mmp.scoring.configuration.MmpJpaTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@EntityScan(basePackages = "de.unistuttgart.ipvs.as.mmp.common.domain")
@Import(MmpJpaTestConfig.class)
public class ScoringRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScoringRepository scoringRepository;

    private Project testProject = Project.builder().name("TestProject").build();
    private Model testModel = Model.builder().project(testProject).build();
    private Scoring testScoring;

    @BeforeEach
    public void setUp() {
        scoringRepository.deleteAll();
        entityManager.persist(testProject);
        entityManager.persist(testModel);
        testScoring = new Scoring(testModel, Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void shouldFindAllScorings() {
        scoringRepository.save(testScoring);

        List<Scoring> scorings = scoringRepository.findAll();
        assertEquals(Collections.singletonList(testScoring), scorings);
    }

    @Test
    public void shouldNotFindAny() {
        List<Scoring> scorings = scoringRepository.findAll();
        assertEquals(Collections.emptyList(), scorings);
    }

    @Test
    public void shouldNotFindById() {
        Optional<Scoring> scoringOptional = scoringRepository.findById(123456789L);
        assertFalse(scoringOptional.isPresent());
    }

    @Test
    public void shouldFindById() {
        Long projectId = scoringRepository.save(testScoring).getId();

        Optional<Scoring> scoring = scoringRepository.findById(projectId);
        assertEquals(Optional.of(testScoring), scoring);
    }
}
