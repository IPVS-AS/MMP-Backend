package de.unistuttgart.ipvs.as.mmp.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;


@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelFile extends BaseEntity {

    @OneToOne(cascade = {CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "dbFile_id")
    @IndexedEmbedded
    private DBFile dbFile;

    @OneToOne
    @JoinColumn(name = "model_id")
    @JsonIgnore
    @ContainedIn
    private Model model;

    private double fileSize;
}
