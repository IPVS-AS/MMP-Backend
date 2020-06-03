package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "evaluation")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Evaluation extends BaseEntity {

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String name;

    @OneToMany(cascade = {CascadeType.ALL})
    @IndexedEmbedded
    private List<Score> scores;
}
