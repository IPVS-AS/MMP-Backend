package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.bridge.builtin.DoubleBridge;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "score")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Score extends BaseEntity {

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String metricName;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    @FieldBridge(impl = DoubleBridge.class)
    private double value;

}
