package de.unistuttgart.ipvs.as.mmp.common.service.impl;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.repository.DBFileRepository;
import de.unistuttgart.ipvs.as.mmp.common.service.DBFileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class DBFileStorageServiceImpl implements DBFileStorageService {
    private final DBFileRepository dbFileRepository;

    public DBFileStorageServiceImpl(DBFileRepository dbFileRepository) {
        this.dbFileRepository = dbFileRepository;
    }

    @Override
    public DBFile storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        DBFile dbFile = new DBFile();
        dbFile.setFileName(fileName);
        dbFile.setFileType(file.getContentType());
        dbFile.setData(file.getBytes());
        return dbFileRepository.save(dbFile);
    }

    @Override
    public Optional<DBFile> getFile(Long fileId) {
        return dbFileRepository.findById(fileId);
    }

    @Override
    public void deleteFile(Long fileId) {
        dbFileRepository.deleteById(fileId);
    }
}
