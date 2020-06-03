package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "dataset")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Dataset extends BaseEntity {

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String type;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String source;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String version;

    @ElementCollection
    @Fields({
            @Field(analyze = Analyze.NO, store = Store.YES),
            @Field(name = "ALL", analyze = Analyze.NO, store = Store.YES)
    })
    @IndexedEmbedded
    private List<String> annotations;

    @OneToMany(cascade = {CascadeType.ALL})
    @LazyCollection(LazyCollectionOption.FALSE)
    @IndexedEmbedded
    private List<InputAttribute> attributes;
}
