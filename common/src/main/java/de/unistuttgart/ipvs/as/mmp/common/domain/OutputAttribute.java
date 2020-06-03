package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "outputattribute")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutputAttribute extends BaseEntity{

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String name;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String resultFeature;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String opType;

}
