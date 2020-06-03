package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "pmmlmetadata")
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PMMLMetadata extends BaseEntity {

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String pmmlVersion;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String description;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String applicationName;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String applicationVersion;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String modelVersion;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String timestamp;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String algorithmName;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String miningFunction;

    @OneToMany(cascade = {CascadeType.ALL})
    @IndexedEmbedded
    private List<InputAttribute> inputAttributes;

    @OneToMany(cascade = {CascadeType.ALL})
    @IndexedEmbedded
    private List<OutputAttribute> outputAttributes;

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fields({
            @Field(analyze = Analyze.NO, store = Store.YES),
            @Field(name = "ALL", analyze = Analyze.NO, store = Store.YES)
    })
    @IndexedEmbedded
    private List<String> pmmlAnnotations;

}
