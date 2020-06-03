package de.unistuttgart.ipvs.as.mmp.common.domain.opcua;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Class which represents OPC UA Object Type Nodes.
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OpcuaObjectTypeNode extends OpcuaObjectNode {

    @JsonIgnore
    @Transient
    private String superTypeNodeId;

    @JsonIgnore
    @Transient
    private OpcuaObjectTypeNode superTypeNode;

    public OpcuaObjectTypeNode(String nodeId, String displayName) {
        super(nodeId, displayName);
    }
}
