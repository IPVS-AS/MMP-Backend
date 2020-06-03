package de.unistuttgart.ipvs.as.mmp.search.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;

import java.util.List;

public interface SearchService {

    List<String> collectAlgorithms();

    List<String> collectSensorNodes();

    List<String> collectMachineNodes();

    List<Model> getModelsByFiltersAndSearchTerms(List<String> searchTerms, List<String> algorithmNames,
                                                 List<String> machineNames, List<String> sensorNames);

}
