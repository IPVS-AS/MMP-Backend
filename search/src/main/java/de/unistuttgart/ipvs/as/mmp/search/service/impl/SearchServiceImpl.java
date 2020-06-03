package de.unistuttgart.ipvs.as.mmp.search.service.impl;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.ModelMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaMetadata;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaObjectNode;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelService;
import de.unistuttgart.ipvs.as.mmp.search.repository.ModelSearchDao;
import de.unistuttgart.ipvs.as.mmp.search.service.SearchService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
public class SearchServiceImpl implements SearchService {

    private static final String NULL_STRING = "_null_";

    private final ModelService modelService;
    private final ModelSearchDao modelSearchDao;

    public SearchServiceImpl(ModelService modelService, ModelSearchDao modelSearchDao) {
        this.modelService = modelService;
        this.modelSearchDao = modelSearchDao;
    }

    @Override
    public List<Model> getModelsByFiltersAndSearchTerms(List<String> searchTerms, List<String> algorithmNames,
                                                        List<String> machineNames, List<String> sensorNames) {
        return modelSearchDao.getModelsByFiltersAndSearchTerms(searchTerms, algorithmNames,
                machineNames, sensorNames);
    }

    @Override
    public List<String> collectAlgorithms() {
        return modelService.getAllModels().stream()
                // if algorithm is null create _null_ value so we can differentiate later between a query for null
                // values and a query where the value does not matter.
                .map(model -> {
                    if (model.getModelMetadata() == null || model.getModelMetadata().getAlgorithm() == null
                            || model.getModelMetadata().getAlgorithm().isEmpty()) {
                        model.setModelMetadata(ModelMetadata.builder().algorithm(NULL_STRING).build());
                    }
                    return model;
                })
                .collect(groupingBy(model -> model.getModelMetadata().getAlgorithm(), counting()))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> collectMachineNodes() {
        return modelService.getAllModels().stream()
                // if datasource, opcua information model or opcua object node is null
                // create _null_ value so we can differentiate later between a query for null
                // values and a query where the value does not matter.
                .map(model -> {
                    if (model.getOpcuaInformationModels() == null && model.getRelationalDBInformation() == null) {
                        model.setOpcuaInformationModels(new ArrayList<>(
                                Arrays.asList(OpcuaInformationModel.builder()

                                        .opcuaMetadata(OpcuaMetadata.builder().machine(
                                                OpcuaObjectNode.builder().displayName(NULL_STRING).build())
                                                .build())
                                        .build()))
                        );
                    }
                    return model;
                })
                .filter(model -> model.getOpcuaInformationModels() != null)
                .map(Model::getOpcuaInformationModels)
                .map(opcuaInformationModels -> {
                    opcuaInformationModels.forEach(opcuaInformationModel -> {
                        if (opcuaInformationModel.getOpcuaMetadata() == null
                                || opcuaInformationModel.getOpcuaMetadata().getMachine() == null
                                || opcuaInformationModel.getOpcuaMetadata().getMachine().getDisplayName().isEmpty()) {
                            opcuaInformationModel.setOpcuaMetadata(
                                    OpcuaMetadata.builder()
                                            .machine(OpcuaObjectNode.builder()
                                                    .displayName(NULL_STRING)
                                                    .build())
                                            .build());
                        }
                    });
                    return opcuaInformationModels;
                })
                .flatMap(opcuaInformationModels -> opcuaInformationModels.stream().map(opcuaInformationModel
                        -> opcuaInformationModel.getOpcuaMetadata().getMachine()))
                .collect(groupingBy(OpcuaObjectNode::getDisplayName, counting()))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> collectSensorNodes() {
        return modelService.getAllModels().stream()
                // if datasource, opcua information model or opcua object node is null
                // create <null> value so we can differentiate later between a query for null
                // values and a query where the value does not matter.
                .map(model -> {
                    if (model.getOpcuaInformationModels() == null && model.getRelationalDBInformation() == null) {
                        model.setOpcuaInformationModels(new ArrayList<>(
                                Arrays.asList(OpcuaInformationModel.builder()
                                        .opcuaMetadata(OpcuaMetadata.builder().sensors(Collections.singletonList(
                                                OpcuaObjectNode.builder().displayName(NULL_STRING).build()))
                                                .build())
                                        .build()))
                        );
                    }
                    return model;
                })
                .filter(model -> model.getOpcuaInformationModels() != null)
                .map(Model::getOpcuaInformationModels)
                .map(opcuaInformationModels -> {
                    opcuaInformationModels.forEach(opcuaInformationModel -> {
                        if (opcuaInformationModel.getOpcuaMetadata() == null
                                || opcuaInformationModel.getOpcuaMetadata().getSensors() == null
                                || opcuaInformationModel.getOpcuaMetadata().getSensors().isEmpty()
                                || opcuaInformationModel.getOpcuaMetadata().getSensors().get(0).getDisplayName().isEmpty()) {
                            opcuaInformationModel.setOpcuaMetadata(
                                    OpcuaMetadata.builder()
                                            .sensors(Collections.singletonList(OpcuaObjectNode.builder()
                                                    .displayName(NULL_STRING)
                                                    .build()))
                                            .build());
                        }
                    });
                    return opcuaInformationModels;
                })
                .flatMap(opcuaInformationModels -> opcuaInformationModels.stream().flatMap(opcuaInformationModel
                        -> opcuaInformationModel.getOpcuaMetadata().getSensors().stream()))
                .collect(groupingBy(OpcuaObjectNode::getDisplayName, counting()))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}
