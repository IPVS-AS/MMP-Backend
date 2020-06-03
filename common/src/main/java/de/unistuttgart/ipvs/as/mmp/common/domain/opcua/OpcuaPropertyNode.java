package de.unistuttgart.ipvs.as.mmp.common.domain.opcua;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * Class which represents OPC UA Property Nodes.
 * Like in the OPC UA Specification it inherits from UA Variable Node.
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OpcuaPropertyNode extends OpcuaVariableNode {

    // The actual value from the Property in the XML information model.
    // e.g. Property Displayname: Einheit    Property value: °C

    public OpcuaPropertyNode(String nodeId, String displayName, String dataType, String value) {
        super(nodeId, displayName, dataType, value);
    }
}
