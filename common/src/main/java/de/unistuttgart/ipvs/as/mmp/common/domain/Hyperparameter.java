package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "hyperparameter")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Hyperparameter extends BaseEntity {
    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String name;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String value;
}
