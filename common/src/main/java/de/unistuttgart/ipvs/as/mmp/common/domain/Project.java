package de.unistuttgart.ipvs.as.mmp.common.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.unistuttgart.ipvs.as.mmp.common.repository.CustomDateBridge;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "project")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity {

    @NotNull
    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String name;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    @Column(length = 1000)
    private String description;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = CustomDateBridge.class)
    private LocalDate creationDate;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = CustomDateBridge.class)
    private LocalDate startDate;

    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = CustomDateBridge.class)
    private LocalDate endDate;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String useCase;

    @Enumerated
    @Field
    private ProjectStatus status;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @JoinTable
    @ApiModelProperty(notes = "List of ids of the users of type Long", dataType = "java.lang.Long")
    @IndexedEmbedded
    private List<User> editors;

    @OneToMany(mappedBy = "project", cascade = {CascadeType.MERGE}, orphanRemoval = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ToString.Exclude
    @ApiModelProperty(notes = "List of ids of the models of type Long", dataType = "java.lang.Long")
    @ContainedIn
    private List<Model> models;
}
