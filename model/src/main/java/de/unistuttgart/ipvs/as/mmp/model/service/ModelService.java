package de.unistuttgart.ipvs.as.mmp.model.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelGroup;

import java.util.List;
import java.util.Optional;

public interface ModelService {

    List<Model> getAllModels();

    List<Model> getAllModelsForProject(Long projectId);

    Optional<Model> getModelForProjectById(Long projectId, Long id);

    Model saveModelForProject(Long projectId, Model model);

    Model updateModelForProjetById(Long projectId, Long id, Model model);

    boolean deleteModelForProjectById(Long projectId, Long id);

    void deleteAllModelsForProject(Long projectId);

    List<ModelGroup> getAllModelGroupsForProject(Long projectId);

    Optional<Model> getLastModelForProjectAndModelGroupIdentifier(Long projectId, Long modelGroupId);

    List<Model> getModelsForProjectAndModelGroupIdentifier(Long projectId, Long modelGroupId);
}