package de.unistuttgart.ipvs.as.mmp.model.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ModelFileService {

    Optional<ModelFile> getModelFileById(Long modelFileId);

    Optional<DBFile> getModelFileRaw(Long modelFileId);

    ModelFile saveModelFile(MultipartFile file, Long modelId, Long projectId);

    boolean deleteModelFile(Long modelFileId, Long modelId, Long projectId);

    Model parseRModelFileInModel(MultipartFile file, Model model);

    Model parsePMMLModelFileInModel(MultipartFile file, Model model);

    Model parsePickleFileInModel(MultipartFile file, Model model);
}
