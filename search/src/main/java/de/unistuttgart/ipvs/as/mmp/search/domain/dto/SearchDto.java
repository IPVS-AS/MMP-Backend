package de.unistuttgart.ipvs.as.mmp.search.domain.dto;

import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchDto {
    private List<String> algorithmsToFilterFor;
    private List<String> machineNamesToFilterFor;
    private List<String> sensorNamesToFilterFor;

    private List<String> searchTerms;
}
