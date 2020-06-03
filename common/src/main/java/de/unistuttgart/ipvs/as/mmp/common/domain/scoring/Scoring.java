package de.unistuttgart.ipvs.as.mmp.common.domain.scoring;

import de.unistuttgart.ipvs.as.mmp.common.domain.BaseEntity;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "scoring")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Scoring extends BaseEntity {
    @ManyToOne(cascade = {CascadeType.ALL})
    private Model model;
    @OneToMany(cascade = {CascadeType.ALL})
    private List<ScoringInput> inputs;
    @OneToMany(cascade = {CascadeType.ALL})
    private List<ScoringOutput> outputs;
}