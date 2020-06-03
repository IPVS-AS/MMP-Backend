package de.unistuttgart.ipvs.as.mmp.model.service.impl;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelGroup;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.Project;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.service.ProjectService;
import de.unistuttgart.ipvs.as.mmp.eam.service.EAMContainerService;
import de.unistuttgart.ipvs.as.mmp.model.repository.ModelRepository;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelService;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModelServiceImpl implements ModelService {

    private final ProjectService projectService;
    private final ModelRepository modelRepository;
    private final EAMContainerService eamContainerService;

    public ModelServiceImpl(ProjectService projectRepository, ModelRepository modelRepository,
                            EAMContainerService eamContainerService) {
        this.projectService = projectRepository;
        this.modelRepository = modelRepository;
        this.eamContainerService = eamContainerService;
    }

    @Override
    public List<Model> getAllModels() {
        return modelRepository.findAll();
    }

    @Override
    public List<Model> getAllModelsForProject(@NonNull Long projectId) {
        if (projectService.existsProjectById(projectId)) {
            return modelRepository.findAllByProjectId(projectId);
        } else {
            throw IdException.idNotFound(Project.class, projectId);
        }
    }

    @Override
    public Optional<Model> getModelForProjectById(@NonNull Long projectId, @NonNull Long modelId) {
        if (projectService.existsProjectById(projectId)) {
            return modelRepository.findById(modelId);
        } else {
            throw IdException.idNotFound(Project.class, projectId);
        }
    }

    @Override
    public Model saveModelForProject(@NonNull Long projectId, @NonNull Model model) {
        Optional<Project> projectOptional = projectService.getProjectById(projectId);
        if (projectOptional.isPresent()) {
            setModelInRelationalDBInformation(model);
            model.setProject(projectOptional.get());
            setModelGroup(model);
            setModelVersion(model);
            modelRepository.save(model);
            return model;
        } else {
            throw IdException.idNotFound(Project.class, projectId);
        }
    }

    @Override
    public Model updateModelForProjetById(@NonNull Long projectId, @NonNull Long modelId, @NonNull Model model) {
        Optional<Model> modelOptional = modelRepository.findById(modelId);
        if (!modelOptional.isPresent()) {
            throw IdException.idNotFound(Model.class, modelId);
        }
        Optional<Project> projectOptional = projectService.getProjectById(projectId);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            Model oldModel = modelOptional.get();
            if (!project.getId().equals(oldModel.getProject().getId())) {
                throw new IllegalArgumentException("Project Id is not the same as Model-Project Id");
            }
            setModelInRelationalDBInformation(model);
            model.setProject(project);
            return modelRepository.save(model);
        } else {
            throw IdException.idNotFound(Project.class, projectId);
        }
    }

    @Override
    public boolean deleteModelForProjectById(@NonNull Long projectId, @NonNull Long modelId) {
        Optional<Project> projectOptional = projectService.getProjectById(projectId);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            Optional<Model> modelOptional = modelRepository.findById(modelId);
            if (modelOptional.isPresent()) {
                if (!project.getId().equals(modelOptional.get().getProject().getId())) {
                    throw new IllegalArgumentException("Project Id is not the same as Model-Project Id");
                }
                Model model = modelOptional.get();
                if (eamContainerService.isModelContainedInEAMContainer(model.getId())) {
                    throw new IllegalArgumentException("Model can't be deleted if it is contained in an EAM Container.");
                }
                model.setProject(projectOptional.get());
                modelRepository.delete(model);
                return true;
            } else {
                return false;
            }
        } else {
            throw IdException.idNotFound(Project.class, projectId);
        }
    }

    @Override
    public void deleteAllModelsForProject(@NonNull Long projectId) {
        Optional<Project> project = projectService.getProjectById(projectId);
        if (project.isPresent()) {
            project.get().getModels().forEach(model -> {
                if (eamContainerService.isModelContainedInEAMContainer(model.getId())) {
                    throw new IllegalArgumentException("At least on model is contained in an EAM Container." +
                            "Can't delete all models in project.");
                }
            });
            modelRepository.deleteByProjectId(projectId);
        } else {
            throw IdException.idNotFound(Project.class, projectId);
        }
    }

    @Override
    public List<ModelGroup> getAllModelGroupsForProject(Long projectId) {
        if (projectService.existsProjectById(projectId)) {
            return modelRepository.findAllModelGroupsByProjectId(projectId);
        } else {
            throw IdException.idNotFound(Project.class, projectId);
        }
    }

    @Override
    public Optional<Model> getLastModelForProjectAndModelGroupIdentifier(Long projectId, Long modelGroupId) {
        if (projectService.existsProjectById(projectId)) {
            return modelRepository.findLastModelByProjectIdAndModelGroupId(projectId, modelGroupId);
        } else {
            throw IdException.idNotFound(Project.class, projectId);
        }
    }

    @Override
    public List<Model> getModelsForProjectAndModelGroupIdentifier(Long projectId, Long modelGroupId) {
        if (projectService.existsProjectById(projectId)) {
            return modelRepository.findAllByProjectIdAndModelMetadata_ModelGroup_id(projectId, modelGroupId);
        } else {
            throw IdException.idNotFound(Project.class, projectId);
        }
    }

    private void setModelInRelationalDBInformation(@NonNull Model model) {
        if (model.getRelationalDBInformation() != null) {
            model.getRelationalDBInformation().setModel(model);
        }
    }

    private void setModelVersion(@NonNull Model model) {
        ModelMetadata modelMetadata = model.getModelMetadata();
        if (modelMetadata != null && modelMetadata.getModelGroup() != null) {
            modelMetadata.setVersion(modelRepository.findMaxModelVersionByModelGroupId(modelMetadata.getModelGroup().getId()).orElse(0L) + 1L);
        }
    }

    private void setModelGroup(@NonNull Model model) {
        ModelMetadata modelMetadata = model.getModelMetadata();
        if (modelMetadata != null) {
            if (modelMetadata.getModelGroup() != null) {
                ModelGroup modelGroup = modelMetadata.getModelGroup();
                Long projectID = model.getProject().getId();
                List<ModelGroup> allModelGroups = modelRepository.findAllModelGroupsByProjectId(projectID);

                String modelGroupName = modelGroup.getModelGroupName();
                if (modelGroupName != null) {
                    // look if the name of model group already exists, if yes then it should be this group
                    // since we don't want two groups with the same name
                    for (ModelGroup existingGroup : allModelGroups) {
                        if (modelGroupName.equals(existingGroup.getModelGroupName())) {
                            modelMetadata.setModelGroup(existingGroup);
                            break;
                        }
                    }
                } else {
                    // If model group name is null, then set modelname as default
                    modelGroup.setModelGroupName(modelMetadata.getName());
                }
            } else {
                throw new IllegalArgumentException("Model group should not be null");
            }
        } else {
            throw new IllegalArgumentException("Model metadata should not be null");
        }
    }
}