package de.unistuttgart.ipvs.as.mmp.common.domain;

import de.unistuttgart.ipvs.as.mmp.common.repository.CustomDateBridge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "training_run")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainingRun extends BaseEntity {

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    @FieldBridge(impl = CustomDateBridge.class)
    private LocalDate startTime;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    @FieldBridge(impl = CustomDateBridge.class)
    private LocalDate endTime;

    @OneToOne(cascade = {CascadeType.ALL})
    @IndexedEmbedded
    private Score currentScore;

    @ElementCollection
    @Fields({
            @Field(analyze = Analyze.NO, store = Store.YES),
            @Field(name = "ALL", analyze = Analyze.NO, store = Store.YES)
    })
    @IndexedEmbedded
    private List<String> annotations;

    @ElementCollection
    @Fields({
            @Field(analyze = Analyze.NO, store = Store.YES),
            @Field(name = "ALL", analyze = Analyze.NO, store = Store.YES)
    })
    @IndexedEmbedded
    private Map<String, String> attributeToValue;

}
