package de.unistuttgart.ipvs.as.mmp.model.repository;


import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long> {

    List<Model> findAllByProjectId(Long projectId);

    @Transactional
    void deleteByProjectId(Long projectId);

    @Query("select max(model.modelMetadata.version) from Model model where model.modelMetadata.modelGroup.id = ?1")
    Optional<Long> findMaxModelVersionByModelGroupId(Long id);

    @Query("select distinct model.modelMetadata.modelGroup from Model model where model.project.id = ?1")
    List<ModelGroup> findAllModelGroupsByProjectId(Long projectId);

    @Query("select model from Model model where model.project.id = ?1 and model.modelMetadata.version = " +
            "(select max(m.modelMetadata.version) from Model m where m.modelMetadata.modelGroup.id = ?2)")
    Optional<Model> findLastModelByProjectIdAndModelGroupId(Long projectId, Long modelGroupId);

    List<Model> findAllByProjectIdAndModelMetadata_ModelGroup_id(Long projectId, Long modelGroupId);
}