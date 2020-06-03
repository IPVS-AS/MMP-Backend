package de.unistuttgart.ipvs.as.mmp.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "relationaldbinformation")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelationalDBInformation extends DataSource {
    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String url;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String dbUser;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String password;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    @JsonIgnore
    @ContainedIn
    private Model model;

    private DataSourceType type = DataSourceType.RELATIONAL_DATA_BASE;

}
