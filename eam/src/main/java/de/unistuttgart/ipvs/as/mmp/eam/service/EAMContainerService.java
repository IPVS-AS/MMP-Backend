package de.unistuttgart.ipvs.as.mmp.eam.service;

import de.unistuttgart.ipvs.as.mmp.eam.domain.EAMContainer;

import java.util.List;

public interface EAMContainerService {
    List<EAMContainer> getAllEAMContainers();

    List<EAMContainer> getAllEAMContainersByModelId(Long modelId);

    boolean isModelContainedInEAMContainer(Long modelId);

    EAMContainer updateEAMContainerById(Long id, EAMContainer eamContainer);

    EAMContainer saveEAMContainer(EAMContainer eamContainer);

    List<EAMContainer> saveEAMContainers(List<EAMContainer> eamContainers);

    boolean deleteEAMContainer(Long id);

    void deleteAllEAMContainer();
}
