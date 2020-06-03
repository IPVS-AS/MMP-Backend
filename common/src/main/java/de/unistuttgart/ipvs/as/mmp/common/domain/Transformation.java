package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "transformation")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transformation extends BaseEntity {

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String name;

    @OneToOne(cascade = {CascadeType.ALL})
    @IndexedEmbedded
    private Dataset input;

    @OneToOne(cascade = {CascadeType.ALL})
    @IndexedEmbedded
    private Dataset output;

    @ElementCollection
    @Fields({
            @Field(analyze = Analyze.NO, store = Store.YES),
            @Field(name = "ALL", analyze = Analyze.NO, store = Store.YES)
    })
    @IndexedEmbedded
    private List<String> parameters;

    @OneToOne(cascade = {CascadeType.ALL})
    @IndexedEmbedded
    private InputAttribute attributeToTransform;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String fromFramework;

}
