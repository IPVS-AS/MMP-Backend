package de.unistuttgart.ipvs.as.mmp.model.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaMetadata;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface OpcuaInformationModelService {

    Optional<OpcuaInformationModel> getOpcuaInformationModelById(Long opcuaInformationModelId);

    Optional<DBFile> getOpcuaInformationModelRaw(Long opcuaInformationModelId);

    OpcuaInformationModel saveOpcuaInformationModel(MultipartFile file, Long modelId, Long projectId,
                                                    String machineTypeId, String sensorTypeId);

    boolean deleteOpcuaInformationModel(Long opcuaInformationModelId, Long projectId, Long modelId);

    OpcuaMetadata parseOpcuaMetadata(InputStream inputStream,
                                     String machineTypeId, String sensorTypeId) throws JAXBException;

    OpcuaInformationModel parseOpcuaInformationModelRaw(MultipartFile file, String machineTypeId, String sensorTypeId);

    OpcuaInformationModel parseAndSaveOpcuaInformationModelRaw(MultipartFile file,
                                                               String machineTypeId, String sensorTypeId);

    List<OpcuaInformationModel> getAllOpcuaInformationModelsByModelAndProjectId(Long projectId, Long modelId);

    boolean deleteAllOpcuaInformationModelsForModel(Long projectId, Long modelId);

}
