package de.unistuttgart.ipvs.as.mmp.common.opcua;

import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.*;
import lombok.Getter;
import org.opcfoundation.ua._2011._03.uanodeset.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A MetaData Parser for OPC UA XML Information Models.
 * It needs a UANodeSet from a parsed OPC UA Information Model.
 */
public class OpcuaMetadataParser {


    public static final String DEFAULT_UAPROPERTYTYPEID = "i=68";
    public static final String DEFAULT_UAVARIABLETYPEID = "i=63";
    public static final String DEFAULT_MACHINETYPEID = "ns=1;i=2027";
    public static final String DEFAULT_SENSORTYPEID = "ns=1;i=1";

    private final String uAPropertyTypeId;
    private final String uAVariableTypeId;
    private final String machineTypeId;
    private final String sensorTypeId;

    private final UANodeSet nodeSet;

    @Getter
    private OpcuaMetadata opcuaMetadata;
    @Getter
    private List<OpcuaObjectNode> nodes;


    /**
     * Creates a default OpcuaMetaDataParser which have as default
     * MACHINETYPEID: "ns=1;i=2027" and SENSORTYPEID: "ns=1;i=1"
     *
     * @param nodeSet the NodeSet which should be parsed.
     */
    public OpcuaMetadataParser(UANodeSet nodeSet) {
        opcuaMetadata = new OpcuaMetadata();
        nodes = new ArrayList<>();
        this.nodeSet = nodeSet;

        this.uAPropertyTypeId = DEFAULT_UAPROPERTYTYPEID;
        this.uAVariableTypeId = DEFAULT_UAVARIABLETYPEID;
        this.machineTypeId = DEFAULT_MACHINETYPEID;
        this.sensorTypeId = DEFAULT_SENSORTYPEID;

        buildInformationSpace();
    }

    /**
     * Creates an OpcuaMetaDataParser with customized Node IDs for MACHINE- and SENSORTYPE.
     *
     * @param nodeSet       the NodeSet which should be parsed.
     * @param machineTypeId the Id from a Machine TypeDefinition Node.
     * @param sensorTypeId  the Id from a Sensor TypeDefinition Node.
     */
    public OpcuaMetadataParser(UANodeSet nodeSet, String machineTypeId, String sensorTypeId) {
        opcuaMetadata = new OpcuaMetadata();
        nodes = new ArrayList<>();
        this.nodeSet = nodeSet;

        this.uAPropertyTypeId = DEFAULT_UAPROPERTYTYPEID;
        this.uAVariableTypeId = DEFAULT_UAVARIABLETYPEID;

        if (machineTypeId != null) {
            this.machineTypeId = machineTypeId;
        } else {
            this.machineTypeId = DEFAULT_MACHINETYPEID;
        }
        if (sensorTypeId != null) {
            this.sensorTypeId = sensorTypeId;
        } else {
            this.sensorTypeId = DEFAULT_SENSORTYPEID;
        }

        buildInformationSpace();
    }

    /**
     * Start building a simplified information space from a parsed XML Information Model.
     */
    private void buildInformationSpace() {
        createNodes();
        assignReferencesToNodes();
        assignMachineAndSensorNodes();
    }

    private void createNodes() {
        // iterate over every node in the XML Information Model and create a simplified OpcuaNode
        for (UANode node : nodeSet.getUAObjectOrUAVariableOrUAMethod()) {
            if (node != null) {

                //-------------------------------------
                // Some characteristics which most Nodes have:
                OpcuaObjectNode newNode;
                String displayName = null;
                String typeDefinitionNodeId = null;
                Reference typeDefinitionReference = OpcuaNodeUtil.getTypeReference(node);

                if (node.getDisplayName().get(0) != null) {
                    displayName = node.getDisplayName().get(0).getValue();
                }

                if (typeDefinitionReference != null) {
                    typeDefinitionNodeId = typeDefinitionReference.getValue();
                }
                //-------------------------------------

                // Create a Property Node.
                if (OpcuaNodeUtil.isNodeFromType(node, uAPropertyTypeId)) {
                    newNode = new OpcuaPropertyNode(node.getNodeId(), displayName,
                            ((UAVariable) node).getDataType(),
                            OpcuaNodeUtil.getValueFromVariable((UAVariable) node));

                }
                // Create a Variable Node.
                else if (OpcuaNodeUtil.isNodeFromType(node, uAVariableTypeId)) {
                    newNode = new OpcuaVariableNode(node.getNodeId(), displayName,
                            ((UAVariable) node).getDataType(), OpcuaNodeUtil.getValueFromVariable((UAVariable) node));


                }
                // Create an Object Type Definition Node.
                else if (node instanceof UAObjectType) {
                    newNode = new OpcuaObjectTypeNode(node.getNodeId(), displayName);

                    // Sets the SuperType of an ObjectType Node if existing.
                    if (OpcuaNodeUtil.getSuperType(node) != null) {
                        ((OpcuaObjectTypeNode) newNode).setSuperTypeNodeId(OpcuaNodeUtil.getSuperType(node).getValue());
                    }
                }
                // Create a normal Object Node.
                else {
                    newNode = new OpcuaObjectNode(node.getNodeId(), node.getDisplayName().get(0).getValue());
                }

                newNode.setTypeDefinitionNodeId(typeDefinitionNodeId);

                // Add Property Nodes.
                List<Reference> propertyReferences = OpcuaNodeUtil.getPropertyReferences(node);
                for (Reference reference : propertyReferences) {
                    newNode.addPropertyId(reference.getValue());
                }

                // Add Component Nodes.
                List<Reference> componentReferences = OpcuaNodeUtil.getComponentReferences(node);
                for (Reference reference : componentReferences) {
                    newNode.addComponentNodeId(reference.getValue());
                }

                nodes.add(newNode);
            }
        }

    }

    private void assignReferencesToNodes() {
        for (OpcuaObjectNode node : nodes) {
            if (node.getTypeDefinitionNodeId() != null) {
                node.setTypeDefinitionNode((OpcuaObjectTypeNode) getNodeById(node.getTypeDefinitionNodeId()));
            }

            if ((node instanceof OpcuaObjectTypeNode) && ((OpcuaObjectTypeNode) node).getSuperTypeNodeId() != null) {
                ((OpcuaObjectTypeNode) node).setSuperTypeNode(
                        (OpcuaObjectTypeNode) getNodeById(((OpcuaObjectTypeNode) node).getSuperTypeNodeId()));
            }

            for (String propertyNodeId : node.getPropertiesIds()) {
                node.addProperty((OpcuaPropertyNode) getNodeById(propertyNodeId));
            }

            for (String componentNodeId : node.getComponentsIds()) {
                OpcuaObjectNode component = getNodeById(componentNodeId);
                if (component instanceof OpcuaVariableNode) {
                    node.addVariable((OpcuaVariableNode) component);
                } else {
                    node.addComponentNode(component);
                }
            }
        }
    }

    private void assignMachineAndSensorNodes() {
        for (OpcuaObjectNode node : nodes) {
            if (!(node instanceof OpcuaObjectTypeNode) && (!(node instanceof OpcuaPropertyNode))) {
                if (node.hasTypeDefinition(sensorTypeId)) {
                    opcuaMetadata.addSensor(node);
                } else if ((opcuaMetadata.getMachine() == null) && (node.hasTypeDefinition(machineTypeId))) {
                    opcuaMetadata.setMachine(node);
                }
            }
        }
    }

    public OpcuaObjectNode getNodeById(String nodeId) {
        if (nodeId != null) {
            for (OpcuaObjectNode node : nodes) {
                if (node.getNodeId().equals(nodeId)) {
                    return node;
                }
            }
        }
        return null;
    }
}
