package de.unistuttgart.ipvs.as.mmp.common.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.NoClass;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Inheritance(strategy=InheritanceType.JOINED)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        defaultImpl = NoClass.class,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OpcuaInformationModel.class, name = "OPCUA"),
        @JsonSubTypes.Type(value = RelationalDBInformation.class, name = "RELATIONAL_DATA_BASE"),
})
public abstract class DataSource extends BaseEntity {

    @Enumerated
    @IndexedEmbedded
    private DataSourceType type;

    public boolean isTypeOf(DataSourceType expectedType) {
        return expectedType.equals(getType());
    }
}
