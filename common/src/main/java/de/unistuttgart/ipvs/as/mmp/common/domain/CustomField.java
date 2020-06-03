package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.*;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "customfields")
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomField extends BaseEntity {
    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String fieldName;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String fieldContent;
}
