package de.unistuttgart.ipvs.as.mmp.eam.service.impl;

import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.eam.domain.BusinessProcess;
import de.unistuttgart.ipvs.as.mmp.eam.repository.BusinessProcessRepository;
import de.unistuttgart.ipvs.as.mmp.eam.service.BusinessProcessService;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class BusinessProcessServiceImpl implements BusinessProcessService {

    private BusinessProcessRepository bpRepository;

    public BusinessProcessServiceImpl(BusinessProcessRepository bpRepository) {
        this.bpRepository = bpRepository;
    }

    @Override
    public List<BusinessProcess> getAllBusinessProcesses() {
        return bpRepository.findAll();
    }

    @Override
    public Optional<BusinessProcess> getBusinessProcessById(@NonNull Long id) {
        return bpRepository.findById(id);
    }

    @Override
    public BusinessProcess saveBusinessProcess(@NonNull BusinessProcess businessProcess) {
        return bpRepository.save(businessProcess);
    }

    @Override
    public List<BusinessProcess> saveBusinessProcesses(@NonNull List<BusinessProcess> businessProcesses) {

        if (businessProcesses.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedList<BusinessProcess> savedBusinessProcesses = new LinkedList<>();
        businessProcesses.forEach(businessProcess -> {
            businessProcess.setPredecessor(null);
            if (!savedBusinessProcesses.isEmpty()) {
                businessProcess.setPredecessor(savedBusinessProcesses.getLast());
            }
            savedBusinessProcesses.add(bpRepository.save(businessProcess));
        });
        return savedBusinessProcesses;

    }

    @Override
    public BusinessProcess updateBusinessProcessById(@NonNull Long id, @NonNull BusinessProcess businessProcess) {
        Optional<BusinessProcess> optionalBP = bpRepository.findById(id);
        BusinessProcess bp = optionalBP
                .orElseThrow(() -> IdException.idNotFound(BusinessProcess.class, id));
        businessProcess.setId(bp.getId());
        return bpRepository.save(businessProcess);
    }

    @Override
    public Optional<BusinessProcess> getBusinessProcessByLabel(@NonNull String name) {
        return bpRepository.findByLabel(name);
    }

    @Override
    public boolean existsBusinessProcessById(@NonNull Long id) {
        return bpRepository.existsById(id);
    }

    @Override
    public boolean deleteBusinessProcessById(@NonNull Long id) {
        if (bpRepository.existsById(id)) {
            bpRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public void deleteAllBusinessProcesses() {
        bpRepository.deleteAll();
    }
}
