package de.unistuttgart.ipvs.as.mmp.common.opcua;

import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaObjectNode;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaPropertyNode;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaVariableNode;
import org.junit.Before;
import org.junit.Test;
import org.opcfoundation.ua._2011._03.uanodeset.ObjectFactory;
import org.opcfoundation.ua._2011._03.uanodeset.UANodeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;


/**
 * @author Marco Link
 */
public class OpcuaMetadataParserTest {

    private OpcuaMetadata opcuaMetadata;
    private ClassLoader classLoader;
    private static final String TEST_FILE_PATH = "opcua/informationModel_example.xml";

    @Before
    public void parseOpcua() throws JAXBException {
        classLoader = this.getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(TEST_FILE_PATH);
        JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
        UANodeSet nodeSet = (UANodeSet) jc.createUnmarshaller().unmarshal(inputStream);
        opcuaMetadata = new OpcuaMetadataParser(nodeSet).getOpcuaMetadata();
    }

    @Test
    public void testOpcuaMetadataMachineNode() {
        OpcuaObjectNode machineNode = opcuaMetadata.getMachine();
        assertEquals("Machine", machineNode.getDisplayName());
        assertEquals(0, machineNode.getComponents().size());
        assertEquals(0, machineNode.getVariables().size());

        OpcuaPropertyNode property1 = opcuaMetadata.getMachine().getProperties().get(0);
        OpcuaPropertyNode property2 = opcuaMetadata.getMachine().getProperties().get(1);

        assertEquals("Seriennummer", property2.getDisplayName());
        assertEquals("Manufacturer", property1.getDisplayName());
        assertEquals(0, property1.getProperties().size());
        assertEquals(0, property2.getProperties().size());
        assertEquals(0, property1.getVariables().size());
        assertEquals(0, property2.getVariables().size());
        assertEquals(0, property1.getComponents().size());
        assertEquals(0, property2.getComponents().size());
        assertEquals("Special Manufacturer", property1.getValue());
        assertEquals("456156188561", property2.getValue());
        assertEquals("String", property1.getDataType());
        assertEquals("String", property2.getDataType());
    }

    @Test
    public void testOpcuaMetadataSensorNode1() {
        OpcuaObjectNode sensor1 = opcuaMetadata.getSensors().get(0);

        assertEquals("LichtSensor", sensor1.getDisplayName());
        assertEquals(3, sensor1.getComponents().size());
        assertEquals(0, sensor1.getProperties().size());
        assertEquals(0, sensor1.getVariables().size());

        OpcuaObjectNode component1 = sensor1.getComponents().get(0);
        OpcuaObjectNode component2 = sensor1.getComponents().get(1);
        OpcuaObjectNode component3 = sensor1.getComponents().get(2);

        // -------------------------------------
        // Component 1
        // -------------------------------------
        assertEquals("Konfiguration", component1.getDisplayName());
        assertEquals(0, component1.getVariables().size());
        assertEquals(0, component1.getComponents().size());
        assertEquals(2, component1.getProperties().size());

        //Property 1
        OpcuaPropertyNode property1 = component1.getProperties().get(0);
        assertEquals("SensorTyp", property1.getDisplayName());
        assertEquals(0, property1.getVariables().size());
        assertEquals(0, property1.getProperties().size());
        assertEquals(0, property1.getComponents().size());
        assertEquals("Licht", property1.getValue());
        assertEquals("String", property1.getDataType());

        // Proeprty 2
        OpcuaPropertyNode property2 = component1.getProperties().get(1);
        assertEquals("IoPin", property2.getDisplayName());
        assertEquals(0, property2.getVariables().size());
        assertEquals(0, property2.getProperties().size());
        assertEquals(0, property2.getComponents().size());
        assertEquals("2", property2.getValue());
        assertEquals("UInt16", property2.getDataType());
        // -------------------------------------

        // -------------------------------------
        // Component 2
        // -------------------------------------
        assertEquals("Lichtstaerke", component2.getDisplayName());
        assertEquals(1, component2.getVariables().size());
        assertEquals(0, component2.getComponents().size());
        assertEquals(1, component2.getProperties().size());

        //Property 1
        property1 = component2.getProperties().get(0);
        assertEquals("Einheit", property1.getDisplayName());
        assertEquals(0, property1.getVariables().size());
        assertEquals(0, property1.getProperties().size());
        assertEquals(0, property1.getComponents().size());
        assertEquals("lx", property1.getValue());
        assertEquals("String", property1.getDataType());

        //Variable 1
        OpcuaVariableNode variable1 = component2.getVariables().get(0);
        assertEquals("Messwert", variable1.getDisplayName());
        assertEquals(0, variable1.getVariables().size());
        assertEquals(0, variable1.getProperties().size());
        assertEquals(0, variable1.getComponents().size());
        assertEquals("0", variable1.getValue());
        assertEquals("UInt32", variable1.getDataType());
        // -------------------------------------

        // -------------------------------------
        // Component 3
        // -------------------------------------
        assertEquals("Stammdaten", component3.getDisplayName());
        assertEquals(0, component3.getVariables().size());
        assertEquals(1, component3.getComponents().size());
        assertEquals(2, component3.getProperties().size());

        // -------------------------------------
        // Component of component 3
        OpcuaObjectNode component3Component = component3.getComponents().get(0);
        assertEquals("Standort", component3Component.getDisplayName());
        assertEquals(2, component3Component.getVariables().size());
        assertEquals(0, component3Component.getComponents().size());
        assertEquals(0, component3Component.getProperties().size());

        // properties of component 3 component

        // property 1
        OpcuaVariableNode component3Variable1 = component3Component.getVariables().get(0);
        assertEquals("Laengengrad", component3Variable1.getDisplayName());
        assertEquals(0, component3Variable1.getVariables().size());
        assertEquals(0, component3Variable1.getProperties().size());
        assertEquals(0, component3Variable1.getComponents().size());
        assertEquals("9.107", component3Variable1.getValue());
        assertEquals("Double", component3Variable1.getDataType());

        // property 2
        OpcuaVariableNode component3Variable2 = component3Component.getVariables().get(1);
        assertEquals("Breitengrad", component3Variable2.getDisplayName());
        assertEquals(0, component3Variable2.getVariables().size());
        assertEquals(0, component3Variable2.getProperties().size());
        assertEquals(0, component3Variable2.getComponents().size());
        assertEquals("48.74518", component3Variable2.getValue());
        assertEquals("Double", component3Variable2.getDataType());

        // -------------------------------------


        //Property 1
        property1 = component3.getProperties().get(0);
        assertEquals("Hersteller", property1.getDisplayName());
        assertEquals(0, property1.getVariables().size());
        assertEquals(0, property1.getProperties().size());
        assertEquals(0, property1.getComponents().size());
        assertEquals("Manufacturer XYZ", property1.getValue());
        assertEquals("String", property1.getDataType());

        // Proeprty 2
        property2 = component3.getProperties().get(1);
        assertEquals("Seriennummer", property2.getDisplayName());
        assertEquals(0, property2.getVariables().size());
        assertEquals(0, property2.getProperties().size());
        assertEquals(0, property2.getComponents().size());
        assertEquals("080312278", property2.getValue());
        assertEquals("String", property2.getDataType());
        // -------------------------------------
    }

    @Test
    public void testOpcuaMetadataSensorNode2() {
        OpcuaObjectNode sensor2 = opcuaMetadata.getSensors().get(1);

        assertEquals("TemperaturSensor", sensor2.getDisplayName());
        assertEquals(3, sensor2.getComponents().size());
        assertEquals(0, sensor2.getProperties().size());
        assertEquals(0, sensor2.getVariables().size());

        OpcuaObjectNode component1 = sensor2.getComponents().get(0);
        OpcuaObjectNode component2 = sensor2.getComponents().get(1);
        OpcuaObjectNode component3 = sensor2.getComponents().get(2);

        // -------------------------------------
        // Component 1
        // -------------------------------------
        assertEquals("Konfiguration", component1.getDisplayName());
        assertEquals(0, component1.getVariables().size());
        assertEquals(0, component1.getComponents().size());
        assertEquals(2, component1.getProperties().size());

        //Property 1
        OpcuaPropertyNode property1 = component1.getProperties().get(0);
        assertEquals("SensorTyp", property1.getDisplayName());
        assertEquals(0, property1.getVariables().size());
        assertEquals(0, property1.getProperties().size());
        assertEquals(0, property1.getComponents().size());
        assertEquals("Temperatur", property1.getValue());
        assertEquals("String", property1.getDataType());

        // Proeprty 2
        OpcuaPropertyNode property2 = component1.getProperties().get(1);
        assertEquals("IoPin", property2.getDisplayName());
        assertEquals(0, property2.getVariables().size());
        assertEquals(0, property2.getProperties().size());
        assertEquals(0, property2.getComponents().size());
        assertEquals("0", property2.getValue());
        assertEquals("UInt16", property2.getDataType());
        // -------------------------------------

        // -------------------------------------
        // Component 2
        // -------------------------------------
        assertEquals("Temperatur", component2.getDisplayName());
        assertEquals(1, component2.getVariables().size());
        assertEquals(0, component2.getComponents().size());
        assertEquals(1, component2.getProperties().size());

        //Property 1
        property1 = component2.getProperties().get(0);
        assertEquals("Einheit", property1.getDisplayName());
        assertEquals(0, property1.getVariables().size());
        assertEquals(0, property1.getProperties().size());
        assertEquals(0, property1.getComponents().size());
        assertEquals("Kelvin", property1.getValue());
        assertEquals("String", property1.getDataType());

        //Variable 1
        OpcuaVariableNode variable1 = component2.getVariables().get(0);
        assertEquals("Messwert", variable1.getDisplayName());
        assertEquals(0, variable1.getVariables().size());
        assertEquals(0, variable1.getProperties().size());
        assertEquals(0, variable1.getComponents().size());
        assertEquals("0.0", variable1.getValue());
        assertEquals("Float", variable1.getDataType());
        // -------------------------------------

        // -------------------------------------
        // Component 3
        // -------------------------------------
        assertEquals("Stammdaten", component3.getDisplayName());
        assertEquals(0, component3.getVariables().size());
        assertEquals(1, component3.getComponents().size());
        assertEquals(2, component3.getProperties().size());

        // -------------------------------------
        // Component of component 3
        OpcuaObjectNode component3Component = component3.getComponents().get(0);
        assertEquals("Standort", component3Component.getDisplayName());
        assertEquals(2, component3Component.getVariables().size());
        assertEquals(0, component3Component.getComponents().size());
        assertEquals(0, component3Component.getProperties().size());

        // properties of component 3 component

        // property 1
        OpcuaVariableNode component3Variable1 = component3Component.getVariables().get(0);
        assertEquals("Laengengrad", component3Variable1.getDisplayName());
        assertEquals(0, component3Variable1.getVariables().size());
        assertEquals(0, component3Variable1.getProperties().size());
        assertEquals(0, component3Variable1.getComponents().size());
        assertEquals("9.107", component3Variable1.getValue());
        assertEquals("Double", component3Variable1.getDataType());

        // property 2
        OpcuaVariableNode component3Variable2 = component3Component.getVariables().get(1);
        assertEquals("Breitengrad", component3Variable2.getDisplayName());
        assertEquals(0, component3Variable2.getVariables().size());
        assertEquals(0, component3Variable2.getProperties().size());
        assertEquals(0, component3Variable2.getComponents().size());
        assertEquals("48.7451", component3Variable2.getValue());
        assertEquals("Double", component3Variable2.getDataType());

        // -------------------------------------

        //Property 1
        property1 = component3.getProperties().get(0);
        assertEquals("Hersteller", property1.getDisplayName());
        assertEquals(0, property1.getVariables().size());
        assertEquals(0, property1.getProperties().size());
        assertEquals(0, property1.getComponents().size());
        assertEquals("Manufacturer ZYX", property1.getValue());
        assertEquals("String", property1.getDataType());

        // Proeprty 2
        property2 = component3.getProperties().get(1);
        assertEquals("Seriennummer", property2.getDisplayName());
        assertEquals(0, property2.getVariables().size());
        assertEquals(0, property2.getProperties().size());
        assertEquals(0, property2.getComponents().size());
        assertEquals("HW75SQx9", property2.getValue());
        assertEquals("String", property2.getDataType());
        // -------------------------------------
    }

}
