package de.unistuttgart.ipvs.as.mmp.common.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.unistuttgart.ipvs.as.mmp.common.repository.CustomDateBridge;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.search.annotations.*;
import org.hibernate.search.bridge.builtin.LongBridge;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "modelmetadata")
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = {"version"}, allowGetters = true)
public class ModelMetadata extends BaseEntity {

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String name;

    @IndexedEmbedded
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH})
    private ModelGroup modelGroup;

    @Fields({
            @Field(),
            @Field(name = "ALL"),
    })
    @FieldBridge(impl = LongBridge.class)
    @Column(updatable = false)
    private Long version;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Fields({
            @Field(analyze = Analyze.NO),
            @Field(name = "ALL", analyze = Analyze.NO)
    })
    @FieldBridge(impl = CustomDateBridge.class)
    private LocalDate lastModified;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Fields({
            @Field(analyze = Analyze.NO),
            @Field(name = "ALL", analyze = Analyze.NO)
    })
    @FieldBridge(impl = CustomDateBridge.class)
    private LocalDate dateOfCreation;

    @Fields({
            @Field(indexNullAs = Field.DEFAULT_NULL_TOKEN),
            @Field(name = "ALL")
    })
    private String algorithm;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    @Column(length = 1000)
    private String modelDescription;

    @OneToOne(cascade = {CascadeType.ALL})
    @IndexedEmbedded
    private PMMLMetadata pmmlMetadata;

    @Enumerated
    @Fields({
            @Field()
    })
    private ModelStatus status;

    @ManyToOne(cascade = {CascadeType.MERGE})
    @IndexedEmbedded
    private User author;

    @OneToMany(cascade = {CascadeType.ALL})
    @LazyCollection(LazyCollectionOption.FALSE)
    @IndexedEmbedded
    private List<Hyperparameter> hyperparameters;

    @OneToMany(cascade = {CascadeType.ALL})
    @LazyCollection(LazyCollectionOption.FALSE)
    @IndexedEmbedded
    private List<CustomField> customFields;

    @OneToMany(cascade = {CascadeType.ALL})
    @LazyCollection(LazyCollectionOption.FALSE)
    @IndexedEmbedded
    private List<TrainingRun> trainingRuns;

    @OneToMany(cascade = {CascadeType.ALL})
    @LazyCollection(LazyCollectionOption.FALSE)
    @IndexedEmbedded
    private List<Transformation> transformations;

    @OneToOne(cascade = {CascadeType.ALL})
    @IndexedEmbedded
    private PredictionMetadata predictionMetadata;

}