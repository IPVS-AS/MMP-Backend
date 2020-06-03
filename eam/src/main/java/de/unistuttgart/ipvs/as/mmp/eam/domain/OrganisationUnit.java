package de.unistuttgart.ipvs.as.mmp.eam.domain;

import de.unistuttgart.ipvs.as.mmp.common.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "organisation_unit")
@Builder
public class OrganisationUnit extends BaseEntity {
    private String label;
    private Integer index;
}
