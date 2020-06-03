package de.unistuttgart.ipvs.as.mmp.common.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface DBFileStorageService {
    DBFile storeFile(MultipartFile file) throws IOException;

    Optional<DBFile> getFile(Long fileId);

    void deleteFile(Long fileId);
}
