package de.unistuttgart.ipvs.as.mmp.eam.service;

import de.unistuttgart.ipvs.as.mmp.eam.domain.BusinessProcess;

import java.util.List;
import java.util.Optional;

public interface BusinessProcessService {

    List<BusinessProcess> getAllBusinessProcesses();

    Optional<BusinessProcess> getBusinessProcessById(Long id);

    BusinessProcess saveBusinessProcess(BusinessProcess businessProcess);

    List<BusinessProcess> saveBusinessProcesses(List<BusinessProcess> businessProcesses);

    BusinessProcess updateBusinessProcessById(Long id, BusinessProcess businessProcess);

    Optional<BusinessProcess> getBusinessProcessByLabel(String name);

    boolean existsBusinessProcessById(Long id);

    boolean deleteBusinessProcessById(Long id);

    void deleteAllBusinessProcesses();

}
