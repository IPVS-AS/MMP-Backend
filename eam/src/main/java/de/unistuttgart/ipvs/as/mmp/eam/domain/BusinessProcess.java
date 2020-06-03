package de.unistuttgart.ipvs.as.mmp.eam.domain;

import de.unistuttgart.ipvs.as.mmp.common.domain.BaseEntity;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "business_process")
@Builder
public class BusinessProcess extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "bp_predecessor_id")
    private BusinessProcess predecessor;

    private String label;
}
