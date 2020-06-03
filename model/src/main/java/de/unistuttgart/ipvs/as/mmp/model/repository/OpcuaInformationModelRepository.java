package de.unistuttgart.ipvs.as.mmp.model.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpcuaInformationModelRepository extends JpaRepository<OpcuaInformationModel, Long> {

    void deleteByModelId(Long modelId);

    List<OpcuaInformationModel> findAllByModelId(Long modelId);

}
