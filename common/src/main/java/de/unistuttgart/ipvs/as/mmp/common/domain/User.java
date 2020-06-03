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
@Table(name = "mmpuser")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String name;

    @Field
    private String role;
    @Field
    private String password;

}