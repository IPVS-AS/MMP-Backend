package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "input_attribute")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InputAttribute extends BaseEntity{

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String name;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String invalidValueReplacement;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String missingValueReplacement;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String usageType;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String dataType;

    @OneToMany(cascade = {CascadeType.ALL})
    private List<Interval> intervals;

    @ElementCollection
    private List<String> possibleValues;
}
