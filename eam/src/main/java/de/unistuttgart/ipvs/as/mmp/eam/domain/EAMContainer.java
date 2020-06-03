package de.unistuttgart.ipvs.as.mmp.eam.domain;

import de.unistuttgart.ipvs.as.mmp.common.domain.BaseEntity;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EAMContainer extends BaseEntity {

    @ManyToOne
    private Model model;

    @ManyToOne
    private BusinessProcess businessProcess;

    @ManyToOne
    private OrganisationUnit organisationUnit;
}
