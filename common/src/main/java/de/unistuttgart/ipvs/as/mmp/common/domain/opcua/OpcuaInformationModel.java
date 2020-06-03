package de.unistuttgart.ipvs.as.mmp.common.domain.opcua;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.DataSource;
import de.unistuttgart.ipvs.as.mmp.common.domain.DataSourceType;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import lombok.*;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "opcuainformationmodels")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpcuaInformationModel extends DataSource {
    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String fileName;

    @OneToOne(cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "dbFile_id")
    @IndexedEmbedded
    private DBFile dbFile;

    @OneToOne(cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "opcuaMetadata_id")
    @IndexedEmbedded(indexNullAs = Field.DEFAULT_NULL_TOKEN)
    private OpcuaMetadata opcuaMetadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    @JsonIgnore
    @ToString.Exclude
    @ContainedIn
    private Model model;

    private DataSourceType type = DataSourceType.OPCUA;
}
