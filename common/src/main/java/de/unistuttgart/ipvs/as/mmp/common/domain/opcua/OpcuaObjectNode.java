package de.unistuttgart.ipvs.as.mmp.common.domain.opcua;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.unistuttgart.ipvs.as.mmp.common.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which represents OPC UA Nodes.
 */
@Data
@Entity
@Table(name = "opcuaobjectnodes")
@Builder
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class OpcuaObjectNode extends BaseEntity {

    public static final int MAXIMUM_DEPTH = 3;

    @JsonIgnore
    private String nodeId;

    @Fields({
            @Field(indexNullAs = Field.DEFAULT_NULL_TOKEN),
            @Field(name = "ALL", indexNullAs = Field.DEFAULT_NULL_TOKEN)
    })
    private String displayName;

    @JsonIgnore
    @Transient
    private List<String> propertiesIds;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @IndexedEmbedded(depth = MAXIMUM_DEPTH)
    private List<OpcuaPropertyNode> properties;

    @JsonIgnore
    @Transient
    private List<String> componentsIds;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @IndexedEmbedded(depth = MAXIMUM_DEPTH)
    private List<OpcuaObjectNode> components;

    @JsonIgnore
    @Transient
    private String typeDefinitionNodeId;

    @JsonIgnore
    @Transient
    private OpcuaObjectTypeNode typeDefinitionNode;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @IndexedEmbedded(depth = MAXIMUM_DEPTH)
    private List<OpcuaVariableNode> variables;

    public OpcuaObjectNode() {
        this.variables = new ArrayList<>();
        this.propertiesIds = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.componentsIds = new ArrayList<>();
        this.components = new ArrayList<>();
    }

    public OpcuaObjectNode(String nodeId, String displayName) {
        this.displayName = displayName;
        this.nodeId = nodeId;
        this.variables = new ArrayList<>();
        this.propertiesIds = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.componentsIds = new ArrayList<>();
        this.components = new ArrayList<>();
    }

    /**
     * Checks whether this OPC UA Node has a given node as Type Definition.
     * Checks it recursively, so it doesn't have to be the direct Type Definition.
     *
     * @param typeDefinitionNodeId The ID from the node for which the relationship should be clarified.
     * @return true if the given node is an direct or indirect Type Defintion, else false.
     */
    public boolean hasTypeDefinition(String typeDefinitionNodeId) {
        if (typeDefinitionNodeId != null) {
            OpcuaObjectTypeNode nextTypeDefinition = typeDefinitionNode;
            while (nextTypeDefinition != null) {
                if (typeDefinitionNodeId.equals(nextTypeDefinition.getNodeId())) {
                    return true;
                } else {
                    nextTypeDefinition = nextTypeDefinition.getSuperTypeNode();
                }
            }
        }
        return false;
    }

    public void addPropertyId(String nodeId) {
        this.propertiesIds.add(nodeId);
    }

    public void addProperty(OpcuaPropertyNode property) {
        this.properties.add(property);
    }

    public void addVariable(OpcuaVariableNode variable) {
        this.variables.add(variable);
    }

    public void addComponentNodeId(String componentNodeId) {
        this.componentsIds.add(componentNodeId);
    }

    public void addComponentNode(OpcuaObjectNode componentNode) {
        this.components.add(componentNode);
    }
}

