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
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "prediction_metadata")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PredictionMetadata extends BaseEntity {

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String annotation;

    @OneToOne(cascade = {CascadeType.ALL})
    @IndexedEmbedded
    private Evaluation evaluation;
}
