package de.unistuttgart.ipvs.as.mmp.model.service.impl;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelFile;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.pmml.PMMLMetadataParser;
import de.unistuttgart.ipvs.as.mmp.common.service.DBFileStorageService;
import de.unistuttgart.ipvs.as.mmp.model.repository.ModelFileRepository;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelFileService;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelService;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ModelFileServiceImpl implements ModelFileService {

    private final ModelFileRepository modelFileRepository;
    private final DBFileStorageService dbFileStorageStorageService;
    private final ModelService modelService;
    private PMMLMetadataParser metadataParser = new PMMLMetadataParser();

    public ModelFileServiceImpl(ModelFileRepository modelFileRepository, DBFileStorageService dbFileStorageStorageService,
                                ModelService modelService) {
        this.modelFileRepository = modelFileRepository;
        this.dbFileStorageStorageService = dbFileStorageStorageService;
        this.modelService = modelService;
    }

    @Override
    public Optional<ModelFile> getModelFileById(@NonNull Long modelFileId) {
        return modelFileRepository.findById(modelFileId);
    }

    @Override
    public Optional<DBFile> getModelFileRaw(@NonNull Long modelFileId) {
        Optional<ModelFile> modelFileOptional = getModelFileById(modelFileId);
        return modelFileOptional.map(ModelFile::getDbFile);
    }

    @Override
    public ModelFile saveModelFile(@NonNull MultipartFile file, @NonNull Long modelId, @NonNull Long projectId) {

        ModelFile modelFile = new ModelFile();
        Optional<Model> modelOptional = modelService.getModelForProjectById(projectId, modelId);
        if (modelOptional.isPresent()) {
            Model model = modelOptional.get();
            if (model.getModelFile() != null && modelFileRepository.existsById(model.getModelFile().getId())
                    && !deleteModelFile(model.getModelFile().getId(), modelId, projectId)) {
                throw new IllegalArgumentException("Failed to delete old ModelFile");
            }
            modelFile.setModel(modelOptional.get());
            long fileSize = file.getSize();
            modelFile.setFileSize(fileSize);
        } else {
            throw IdException.idNotFound(Model.class, modelId);
        }
        try {
            DBFile dbFile = dbFileStorageStorageService.storeFile(file);
            modelFile.setDbFile(dbFile);
            return modelFileRepository.save(modelFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save File");
        }
    }

    @Override
    public boolean deleteModelFile(Long modelFileId, Long modelId, Long projectId) {
        Optional<ModelFile> modelFileOptional = getModelFileById(modelFileId);
        if (modelFileOptional.isPresent()) {
            Optional<Model> optionalModel = modelService.getModelForProjectById(projectId, modelId);
            if (optionalModel.isPresent()) {
                modelFileRepository.deleteById(modelFileId);
                return true;
            }
        }
        return false;

    }

    @Override
    public Model parseRModelFileInModel(@NonNull MultipartFile file, @NonNull Model model) {
        try {
            List<Model> modelList = metadataParser.parseRFile(file.getInputStream(), model.getModelMetadata());
            return createModelFromModelList(modelList, model);
        } catch (IOException e) {
            throw new IllegalArgumentException("Parsing error");
        }
    }

    private Model createModelFromModelList(List<Model> modelList, Model model) {
        if (modelList.size() > 1 || modelList.isEmpty()) {
            throw new IllegalArgumentException("Multiple or null Models in PMML File.");
        }
        ModelFile modelFile = new ModelFile();
        model.setModelMetadata(modelList.get(0).getModelMetadata());
        model.setModelFile(modelFile);
        return model;
    }

    @Override
    public Model parsePMMLModelFileInModel(@NonNull MultipartFile file, @NonNull Model model) {
        try {
            List<Model> modelList = metadataParser.parsePMMLFile(file.getInputStream(), model.getModelMetadata());
            return createModelFromModelList(modelList, model);
        } catch (JAXBException | SAXException | IOException e) {
            throw new IllegalArgumentException("Parsing error");
        }
    }

    @Override
    public Model parsePickleFileInModel(MultipartFile file, Model model) {
        try {
            List<Model> modelList = metadataParser.parsePickleFile(file.getInputStream(), model.getModelMetadata());
            return createModelFromModelList(modelList, model);
        } catch (IOException e) {
            throw new IllegalArgumentException("Parsing error");
        }
    }
}
