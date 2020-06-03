package de.unistuttgart.ipvs.as.mmp.common.domain.opcua;

import de.unistuttgart.ipvs.as.mmp.common.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which represents OPC UA MetaData from a Machine with Sensors.
 */
@Entity
@Table(name = "opcuametadata")
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OpcuaMetadata extends BaseEntity {

    @OneToOne(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @IndexedEmbedded(indexNullAs = Field.DEFAULT_NULL_TOKEN)
    private OpcuaObjectNode machine;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<OpcuaObjectNode> sensors;

    public OpcuaMetadata() {
        sensors = new ArrayList<>();
    }

    public void addSensor(OpcuaObjectNode sensorNode) {
        this.sensors.add(sensorNode);
    }

    @IndexedEmbedded(indexNullAs = Field.DEFAULT_NULL_TOKEN)
    public List<OpcuaObjectNode> getSensors() {
        if (this.sensors == null || this.sensors.isEmpty()) {
            return null;
        }
        return this.sensors;
    }

}
