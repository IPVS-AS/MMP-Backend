package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "model_group")
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelGroup extends BaseEntity{
    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    @Column(updatable = false)
    private String modelGroupName;
}
