package de.unistuttgart.ipvs.as.mmp.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "interval")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Interval extends BaseEntity{
    private Double startRange;
    private Double endRange;
    private String closure;
}
