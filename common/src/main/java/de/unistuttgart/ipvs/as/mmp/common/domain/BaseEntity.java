package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    private Long id;
}