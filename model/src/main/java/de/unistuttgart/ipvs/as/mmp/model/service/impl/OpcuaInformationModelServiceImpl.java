package de.unistuttgart.ipvs.as.mmp.model.service.impl;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaMetadata;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.opcua.OpcuaMetadataParser;
import de.unistuttgart.ipvs.as.mmp.common.service.DBFileStorageService;
import de.unistuttgart.ipvs.as.mmp.model.repository.OpcuaInformationModelRepository;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelService;
import de.unistuttgart.ipvs.as.mmp.model.service.OpcuaInformationModelService;
import lombok.NonNull;
import org.opcfoundation.ua._2011._03.uanodeset.ObjectFactory;
import org.opcfoundation.ua._2011._03.uanodeset.UANodeSet;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
public class OpcuaInformationModelServiceImpl implements OpcuaInformationModelService {

    private final OpcuaInformationModelRepository opcuaInformationModelRepository;
    private final DBFileStorageService dbFileStorageStorageService;
    private final ModelService modelService;

    public OpcuaInformationModelServiceImpl(OpcuaInformationModelRepository opcuaInformationModelRepository,
                                            DBFileStorageService dbFileStorageService,
                                            ModelService modelService) {
        this.opcuaInformationModelRepository = opcuaInformationModelRepository;
        this.dbFileStorageStorageService = dbFileStorageService;
        this.modelService = modelService;
    }

    @Override
    public Optional<OpcuaInformationModel> getOpcuaInformationModelById(@NonNull Long opcuaInformationModelId) {
        return opcuaInformationModelRepository.findById(opcuaInformationModelId);
    }


    @Override
    public Optional<DBFile> getOpcuaInformationModelRaw(@NonNull Long opcuaInformationModelId) {
        Optional<OpcuaInformationModel> optionalOpcuaInformationModel
                = getOpcuaInformationModelById(opcuaInformationModelId);

        return optionalOpcuaInformationModel.map(OpcuaInformationModel::getDbFile);
    }

    @Override
    public OpcuaInformationModel saveOpcuaInformationModel(@NonNull MultipartFile file,
                                                           @NonNull Long projectId,
                                                           @NonNull Long modelId,
                                                           String machineTypeId, String sensorTypeId) {

        OpcuaInformationModel opcuaInformationModel;
        Optional<Model> modelOptional = modelService.getModelForProjectById(projectId, modelId);
        if (modelOptional.isPresent()) {
            Model model = modelOptional.get();
            opcuaInformationModel = parseAndSaveOpcuaInformationModelRaw(file, machineTypeId, sensorTypeId);
            opcuaInformationModel.setModel(model);
        } else {
            throw IdException.idNotFound(Model.class, modelId);
        }
        return opcuaInformationModelRepository.save(opcuaInformationModel);
    }

    @Override
    public OpcuaMetadata parseOpcuaMetadata(@NonNull InputStream inputStream,
                                            String machineTypeId, String sensorTypeId) throws JAXBException {

        JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
        UANodeSet nodeSet = (UANodeSet) jc.createUnmarshaller().unmarshal(inputStream);
        return new OpcuaMetadataParser(nodeSet, machineTypeId, sensorTypeId).getOpcuaMetadata();
    }

    @Override
    public OpcuaInformationModel parseOpcuaInformationModelRaw(@NonNull MultipartFile file,
                                                               String machineTypeId,
                                                               String sensorTypeId) {

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        OpcuaInformationModel opcuaInformationModel = new OpcuaInformationModel();
        opcuaInformationModel.setFileName(fileName);

        try {
            opcuaInformationModel.setOpcuaMetadata(
                    parseOpcuaMetadata(file.getInputStream(), machineTypeId, sensorTypeId));
            return opcuaInformationModel;
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Failed to parse OPC UA Information Model.");
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to open file.");
        }
    }

    @Override
    public OpcuaInformationModel parseAndSaveOpcuaInformationModelRaw(@NonNull MultipartFile file, String machineTypeId, String sensorTypeId) {
        OpcuaInformationModel opcuaInformationModel = parseOpcuaInformationModelRaw(file, machineTypeId, sensorTypeId);

        try {
            DBFile dbFile = dbFileStorageStorageService.storeFile(file);
            opcuaInformationModel.setDbFile(dbFile);
            return opcuaInformationModel;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save File.");
        }
    }

    @Override
    public List<OpcuaInformationModel> getAllOpcuaInformationModelsByModelAndProjectId(@NonNull Long projectId, @NonNull Long modelId) {

        Optional<Model> optionalModel = modelService.getModelForProjectById(projectId, modelId);
        if (!optionalModel.isPresent()) {
            throw IdException.idNotFound(Model.class, modelId);
        }
        return opcuaInformationModelRepository.findAllByModelId(modelId);
    }

    @Override
    public boolean deleteAllOpcuaInformationModelsForModel(Long projectId, Long modelId) {

        Optional<Model> optionalModel = modelService.getModelForProjectById(projectId, modelId);
        if (!optionalModel.isPresent()) {
            throw IdException.idNotFound(Model.class, modelId);
        }
        opcuaInformationModelRepository.deleteByModelId(modelId);
        return true;
    }

    @Override
    public boolean deleteOpcuaInformationModel(@NonNull Long opcuaInformationModelId,
                                               @NonNull Long projectId,
                                               @NonNull Long modelId) {

        Optional<Model> modelOptional = modelService.getModelForProjectById(projectId, modelId);
        if (!modelOptional.isPresent()) {
            throw IdException.idNotFound(Model.class, modelId);
        }
        Optional<OpcuaInformationModel> optionalOpcuaInformationModel =
                getOpcuaInformationModelById(opcuaInformationModelId);
        if (!optionalOpcuaInformationModel.isPresent()) {
            throw IdException.idNotFound(OpcuaInformationModel.class, opcuaInformationModelId);
        }

        OpcuaInformationModel opcuaInformationModel = optionalOpcuaInformationModel.get();
        if (!modelId.equals(opcuaInformationModel.getModel().getId())) {
            throw new IllegalArgumentException("Model id is not the same as the Model id of the opcua information model");
        }

        opcuaInformationModelRepository.deleteById(opcuaInformationModelId);
        return true;
    }
}
