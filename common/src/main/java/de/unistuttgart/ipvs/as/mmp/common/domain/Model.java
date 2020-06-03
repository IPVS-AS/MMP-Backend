package de.unistuttgart.ipvs.as.mmp.common.domain;

import com.fasterxml.jackson.annotation.*;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.Scoring;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.lucene.analysis.core.KeywordTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "model")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = {"projectId", "opcuaInformationModels", "modelFile"}, allowGetters = true)
@Indexed
@AnalyzerDef(name = "customAnalyzer",
        tokenizer = @TokenizerDef(factory = KeywordTokenizerFactory.class),
        filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class)
        })
@Analyzer(definition = "customAnalyzer")
public class Model extends BaseEntity {

    @OneToOne(cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "modelmetadata_id")
    @IndexedEmbedded
    private ModelMetadata modelMetadata;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonProperty("projectId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @IndexedEmbedded
    private Project project;

    @OneToMany(mappedBy = "model", cascade = {CascadeType.REMOVE})
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ApiModelProperty(notes = "List of ids of the opcua information models of type Long", dataType = "java.lang.Long")
    @ToString.Exclude
    private List<OpcuaInformationModel> opcuaInformationModels;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "model", orphanRemoval = true)
    @JoinColumn(name = "relational_db_information_id")
    @IndexedEmbedded(indexNullAs = Field.DEFAULT_NULL_TOKEN)
    private RelationalDBInformation relationalDBInformation;

    @OneToOne(cascade = CascadeType.REMOVE, mappedBy = "model")
    @JsonProperty("modelFile")
    @IndexedEmbedded
    private ModelFile modelFile;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Scoring> scorings;

    @IndexedEmbedded(indexNullAs = Field.DEFAULT_NULL_TOKEN)
    public List<OpcuaInformationModel> getOpcuaInformationModels() {
        if (this.opcuaInformationModels == null || this.opcuaInformationModels.isEmpty()) {
            return null;
        }
        return this.opcuaInformationModels;
    }
}
