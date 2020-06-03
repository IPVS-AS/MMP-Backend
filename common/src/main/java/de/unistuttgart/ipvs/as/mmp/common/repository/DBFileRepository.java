package de.unistuttgart.ipvs.as.mmp.common.repository;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DBFileRepository extends JpaRepository<DBFile, Long> {
}
