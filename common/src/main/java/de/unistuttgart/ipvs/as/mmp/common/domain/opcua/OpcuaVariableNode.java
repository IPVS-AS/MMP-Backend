package de.unistuttgart.ipvs.as.mmp.common.domain.opcua;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;

import javax.persistence.Entity;

/**
 * Class which represents OPC UA Variable Nodes.
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OpcuaVariableNode extends OpcuaObjectNode {

    // The data type of the Variable which is specified in the OPC UA information model.
    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String dataType;

    @Fields({
            @Field(),
            @Field(name = "ALL")
    })
    private String value;

    public OpcuaVariableNode(String nodeId, String displayName, String dataType) {
        super(nodeId, displayName);
        this.dataType = dataType;
    }

    public OpcuaVariableNode(String nodeId, String displayName, String dataType, String value) {
        super(nodeId, displayName);
        this.dataType = dataType;
        this.value = value;
    }
}
