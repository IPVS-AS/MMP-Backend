package de.unistuttgart.ipvs.as.mmp.search.domain.dto;

import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterDto {

    private List<String> possibleAlgorithmsToFilter;
    private List<String> possibleMachineNamesToFilter;
    private List<String> possibleSensorNamesToFilter;
}
