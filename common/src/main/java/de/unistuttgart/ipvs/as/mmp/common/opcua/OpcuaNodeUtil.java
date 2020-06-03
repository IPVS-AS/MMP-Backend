package de.unistuttgart.ipvs.as.mmp.common.opcua;

import org.opcfoundation.ua._2011._03.uanodeset.Reference;
import org.opcfoundation.ua._2011._03.uanodeset.UANode;
import org.opcfoundation.ua._2011._03.uanodeset.UAVariable;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for retrieving informations regarding UANode, UAVariable, Reference, UAProperty
 * from the JAXB parsed XML information model.
 */
public class OpcuaNodeUtil {

    private OpcuaNodeUtil() {
    }

    /**
     * For a given UAVariable it will return the in the XML information model specified value.
     * Note that a Property in OPC UA is also a Variable.
     *
     * @param uAVariable the variable for which the value should be returned.
     * @return the value as String.
     */
    public static String getValueFromVariable(UAVariable uAVariable) {
        if ((uAVariable.getValue() != null) && (uAVariable.getValue().getAny() != null) &&
                (uAVariable.getValue().getAny() instanceof Element)) {
            Element element = (Element) uAVariable.getValue().getAny();
            return element.getChildNodes().item(0).getNodeValue();
        }
        return "";
    }

    /**
     * For a given UANode, a specified Reference Type and whether the reference is forward or not,
     * a List with all matching Reference Objects will be returned.
     *
     * @param node          the Node for which the references should be found.
     * @param referenceType the Reference Type to search for. (e.g. HasSubtype)
     * @param isForward     whether the Reference is forward or not.
     * @return a List with found references. Can be empty if no specified references present.
     */
    public static List<Reference> getReferences(UANode node, String referenceType, boolean isForward) {
        List<Reference> references = new ArrayList<>();
        for (Reference reference : node.getReferences().getReference()) {
            if ((reference.isIsForward() == isForward) &&
                    (reference.getReferenceType().toLowerCase().equalsIgnoreCase(referenceType))) {
                references.add(reference);
            }
        }
        return references;
    }

    /**
     * For a given UANode all forward headed Component References will be returned.
     *
     * @param node the Node for which the References should be found.
     * @return a List with all found References. Can be empty if no specified references present.
     */
    public static List<Reference> getComponentReferences(UANode node) {
        List<Reference> references = getReferences(node, "hascomponent", true);
        references.addAll(getReferences(node, "organizes", true));
        return references;
    }

    /**
     * For a given UANode all forward headed Property References will be returned.
     *
     * @param node the Node for which the References should be found.
     * @return a List with all found References. Can be empty if no specified references present.
     */
    public static List<Reference> getPropertyReferences(UANode node) {
        return getReferences(node, "hasproperty", true);
    }

    /**
     * For a given UANode the Type Definition Reference will be returned.
     *
     * @param node the Node for which the Reference should be found.
     * @return the Reference or null if not present.
     */
    public static Reference getTypeReference(UANode node) {
        for (Reference reference : node.getReferences().getReference()) {
            if ((reference.isIsForward()) &&
                    (reference.getReferenceType().equalsIgnoreCase("hastypedefinition"))) {
                return reference;
            }
        }
        return null;
    }

    /**
     * For a given UANode the Super Type Reference will be returned.
     *
     * @param node the Node for which the Reference should be found.
     * @return the Reference or null if not present.
     */
    public static Reference getSuperType(UANode node) {
        List<Reference> references = getReferences(node, "hassubtype", false);
        if (!references.isEmpty()) {
            return references.get(0);
        }
        return null;
    }

    /**
     * For a given UANode and a specified NodeId it will be looked, whether the specified NodeId is a supertype.
     *
     * @param node        the Node for which the SuperType should be checked.
     * @param supertypeId the Id from the Node which is probably the SuperType.
     * @return true if it is SuperType, else false.
     */
    public static boolean hasNodeSupertype(UANode node, String supertypeId) {
        if ((supertypeId != null) && (node != null)) {
            Reference superTypeReference = getSuperType(node);
            if ((superTypeReference != null) &&
                    (superTypeReference.getValue().equalsIgnoreCase(supertypeId))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a given UANode has the given NodeId as Type Definition.
     *
     * @param node       the Node which Type Definition should be checked.
     * @param nodeTypeId the Id from the Node which is probably the Type Definition.
     * @return true if it is the Type Definition, else false.
     */
    public static boolean isNodeFromType(UANode node, String nodeTypeId) {
        Reference typeReference = getTypeReference(node);
        if (((typeReference != null) && (typeReference.getValue() != null))) {
            return typeReference.getValue().equals(nodeTypeId);
        }
        return false;
    }
}
