package de.unistuttgart.ipvs.as.mmp.eam.service.impl;

import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.eam.domain.EAMContainer;
import de.unistuttgart.ipvs.as.mmp.eam.repository.EAMContainerRepository;
import de.unistuttgart.ipvs.as.mmp.eam.service.EAMContainerService;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Service
public class EAMContainerServiceImpl implements EAMContainerService {

    private final EAMContainerRepository eamContainerRepository;

    public EAMContainerServiceImpl(EAMContainerRepository eamContainerRepository) {
        this.eamContainerRepository = eamContainerRepository;
    }

    @Override
    public List<EAMContainer> getAllEAMContainers() {
        return eamContainerRepository.findAll();
    }

    @Override
    public List<EAMContainer> getAllEAMContainersByModelId(@NonNull Long modelId) {
        return eamContainerRepository.findAllByModel_Id(modelId);
    }

    @Override
    public boolean isModelContainedInEAMContainer(@NonNull Long modelId) {
        return !CollectionUtils.isEmpty(getAllEAMContainersByModelId(modelId));
    }

    @Override
    public EAMContainer updateEAMContainerById(@NonNull Long id, @NonNull EAMContainer eamContainer) {
        Optional<EAMContainer> optionalEAMContainer = eamContainerRepository.findById(id);
        if (!optionalEAMContainer.isPresent()) {
            throw IdException.idNotFound(EAMContainer.class, id);
        }
        return eamContainerRepository.save(eamContainer);
    }

    @Override
    public EAMContainer saveEAMContainer(@NonNull EAMContainer eamContainer) {
        return eamContainerRepository.save(eamContainer);
    }

    @Override
    public List<EAMContainer> saveEAMContainers(@NonNull List<EAMContainer> eamContainers) {
        return eamContainerRepository.saveAll(eamContainers);
    }

    @Override
    public boolean deleteEAMContainer(@NonNull Long id) {
        Optional<EAMContainer> optionalEAMContainer = eamContainerRepository.findById(id);
        if (optionalEAMContainer.isPresent()) {
            eamContainerRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void deleteAllEAMContainer() {
        eamContainerRepository.deleteAll();
    }
}
