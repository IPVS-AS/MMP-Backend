package de.unistuttgart.ipvs.as.mmp.init;

import com.google.common.io.ByteStreams;
import de.unistuttgart.ipvs.as.mmp.common.domain.*;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaInformationModel;
import de.unistuttgart.ipvs.as.mmp.common.domain.opcua.OpcuaMetadata;
import de.unistuttgart.ipvs.as.mmp.common.pmml.PMMLMetadataParser;
import de.unistuttgart.ipvs.as.mmp.common.repository.DBFileRepository;
import de.unistuttgart.ipvs.as.mmp.common.repository.ProjectRepository;
import de.unistuttgart.ipvs.as.mmp.common.repository.UserRepository;
import de.unistuttgart.ipvs.as.mmp.common.util.DefaultDataBuilder;
import de.unistuttgart.ipvs.as.mmp.eam.domain.BusinessProcess;
import de.unistuttgart.ipvs.as.mmp.eam.domain.EAMContainer;
import de.unistuttgart.ipvs.as.mmp.eam.domain.OrganisationUnit;
import de.unistuttgart.ipvs.as.mmp.eam.repository.BusinessProcessRepository;
import de.unistuttgart.ipvs.as.mmp.eam.repository.EAMContainerRepository;
import de.unistuttgart.ipvs.as.mmp.eam.repository.OrganisationUnitRepository;
import de.unistuttgart.ipvs.as.mmp.model.repository.ModelFileRepository;
import de.unistuttgart.ipvs.as.mmp.model.repository.ModelRepository;
import de.unistuttgart.ipvs.as.mmp.model.repository.OpcuaInformationModelRepository;
import de.unistuttgart.ipvs.as.mmp.model.service.OpcuaInformationModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Transactional
public class DatabaseInitializer implements ApplicationListener<ApplicationStartedEvent> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${database.production.init}")
    private boolean isInitProductionData;

    private final ModelRepository modelRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ModelFileRepository modelFileRepository;
    private final BusinessProcessRepository businessProcessRepository;
    private final OrganisationUnitRepository organisationUnitRepository;
    private final EAMContainerRepository eamContainerRepository;
    private final DBFileRepository dbFileRepository;
    private final OpcuaInformationModelService opcuaInformationModelService;
    private final OpcuaInformationModelRepository opcuaInformationModelRepository;
    private PMMLMetadataParser pmmlMetadataParser = new PMMLMetadataParser();
    private ResourceLoader resourceLoader;

    private static final String[] projectUseCases = {"Predictive Maintenance", "Predictive Quality",
            "Root Cause Analysis", "Lead Time Improvement"};

    private static final List<String> projectDescriptionPrefixes = Arrays.asList("New", "Old", "Best", "Even better",
            "Rather bad", "Medium", "Excellent", "Test", "Mediocre", "Rather good", "An even better", "Special",
            "Slightly better", "Perfect", "An even worse", "Not sure about that");

    private static final List<String> modelNamePrefixes = Arrays.asList("Predictive", "Clustering", "Best", "Even better",
            "Rather bad", "Medium", "Excellent", "Test", "Mediocre", "Super-slow", "Rather good", "An even better",
            "Performant", "Special", "Not usable", "Usable", "Slightly better", "Perfect", "Super-fast",
            "An even worse", "Not sure about that", "Still better than the previous", "Accidentally created");

    private static final List<String> algorithmNames = Arrays.asList("J48", "Simple Logistic Regression", "rpart", "randomForest",
            "RProp", "Naive Bayes", "Multilayer Perceptron", "Linear Regression", "Hoeffding Tree");

    private static final String[] eamBusinessProcesses = {"Development", "Purchasing", "Marketing", "Acquire orders",
            "Production", "Sales", "Shipping", "After Sales"};

    private static final String[] orgUnitNames = {"Neukirch", "Berlin", "Ditzingen", "Karlsruhe", "Teningen", "Aachen",
            "München-Unterföhring", "Hettingen", "Gerlingen", "Stuttgart"};

    private static final String[] opcuaFiles = {"informationModel_laserMachine.xml",
            "informationModel_laserWeldingMachine.xml", "informationModel_laserWeldingMachine2.xml"};

    private static final String[] pmmlFiles = {"AuditTree.xml", "IrisRandomForest.xml", "iristree_example.xml",
            "single_audit_mlp.xml"};

    public DatabaseInitializer(ModelRepository modelRepository, UserRepository userRepository,
                               ProjectRepository projectRepository, ModelFileRepository modelFileRepository,
                               DBFileRepository dbFileRepository, BusinessProcessRepository businessProcessRepository,
                               OrganisationUnitRepository organisationUnitRepository, EAMContainerRepository eamContainerRepository,
                               OpcuaInformationModelService opcuaInformationModelService,
                               OpcuaInformationModelRepository opcuaInformationModelRepository, ResourceLoader resourceLoader) {
        this.modelRepository = modelRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.dbFileRepository = dbFileRepository;
        this.modelFileRepository = modelFileRepository;
        this.businessProcessRepository = businessProcessRepository;
        this.organisationUnitRepository = organisationUnitRepository;
        this.eamContainerRepository = eamContainerRepository;
        this.opcuaInformationModelService = opcuaInformationModelService;
        this.opcuaInformationModelRepository = opcuaInformationModelRepository;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        switch (activeProfile) {
            case "production":
                log.info("Application is running in production mode.");
                break;
            case "local":
                log.info("Application is running non production mode.");
                createDummyData();
                break;
            case "productionInit":
                log.info("Application is running production init mode.");
                createProductionData();
                break;
            default:
                log.info("Application is running on a non knowing mode");
                break;
        }
    }

    private void createProductionData() {
        createDummyData();
    }

    /**
     * We do not need to clean the DB, it is create-drop by default
     */
    private void createDummyData() {
        List<User> users = createUsers();
        List<Project> projects = createProjects(users);
        userRepository.saveAll(users);
        projectRepository.saveAll(projects);
        List<Model> models = createForEachModelStatusModel(projects, users);
        createEAMContainer(models);
        createModelFile(models);
        createOpcuaFile(models);
    }

    private List<User> createUsers() {
        User user = DefaultDataBuilder.userBuilder.build();
        User user1 = DefaultDataBuilder.userBuilder
                .name("Alice")
                .build();
        User user2 = DefaultDataBuilder.userBuilder
                .name("Jill")
                .build();
        return Arrays.asList(user, user1, user2);
    }

    private List<Project> createProjects(List<User> users) {
        List<Project> projects = new ArrayList<>();
        List<ProjectStatus> projectStatuses = Arrays.asList(ProjectStatus.values());

        Collections.shuffle(projectDescriptionPrefixes);

        for (int i = 0; i < projectUseCases.length; i++) {
            LocalDate startDate = getRandomDate(LocalDate.now().minusYears(18L));
            LocalDate creationDate = getRandomDate(startDate);
            LocalDate endDate = getRandomDate(creationDate);

            String description = "description for Enpro Project" + (i + 1);
            if (i < projectDescriptionPrefixes.size()) {
                description = projectDescriptionPrefixes.get(i) + " " + description;
            }

            Project project = DefaultDataBuilder.projetBuilder
                    .name(projectUseCases[i])
                    .useCase(projectUseCases[i])
                    .description(description)
                    .editors(Collections.singletonList(getRandomElement(users)))
                    .status(getRandomElement(projectStatuses))
                    .creationDate(creationDate)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
            projects.add(project);
        }

        return projects;
    }

    private List<Model> createForEachModelStatusModel(List<Project> projects, List<User> users) {
        List<Model> models = new ArrayList<>();
        List<ModelStatus> modelStatuses = Arrays.asList(ModelStatus.values());

        projects.forEach(project -> {
            int amountModelGroups = ThreadLocalRandom.current().nextInt(3, 8);
            project.setModels(new ArrayList<>());

            Collections.shuffle(modelNamePrefixes);
            String modelBaseName = "Model for " + project.getUseCase();

            for (int modelGroupIndex = 0; modelGroupIndex < amountModelGroups; modelGroupIndex++) {

                String modelName = modelBaseName;
                if (modelGroupIndex < modelNamePrefixes.size()) {
                    modelName = modelNamePrefixes.get(modelGroupIndex) + " " + modelBaseName;
                }

                String algorithm = getRandomElement(algorithmNames);

                int amountModelsPerModelGroup = ThreadLocalRandom.current().nextInt(1, 10);
                ModelGroup modelGroup = ModelGroup.builder()
                        .modelGroupName(modelName).build();

                LocalDate dateOfCreation = getRandomDate(LocalDate.now().minusYears(18L));
                LocalDate lastModified = getRandomDate(dateOfCreation);

                for (long modelIndex = 1; modelIndex <= amountModelsPerModelGroup; modelIndex++) {
                    ModelMetadata modelMetadata = DefaultDataBuilder.getNewMetadataBuilder()
                            .name(modelName + modelIndex)
                            .author(getRandomElement(users))
                            .version(modelIndex)
                            .status(getRandomElement(modelStatuses))
                            .algorithm(algorithm)
                            .dateOfCreation(dateOfCreation)
                            .lastModified(lastModified)
                            .modelGroup(modelGroup)
                            .build();
                    Model model = DefaultDataBuilder.modelBuilder
                            .project(project)
                            .modelMetadata(modelMetadata)
                            .build();

                    if (ThreadLocalRandom.current().nextInt(5) == 0) {
                        model.setRelationalDBInformation(RelationalDBInformation.builder().url("http://enpro.de:8080")
                                .dbUser("testUser").password("password").type(DataSourceType.RELATIONAL_DATA_BASE)
                                .model(model).build());
                    }

                    modelRepository.save(model);
                    models.add(model);

                    dateOfCreation = getRandomDate(lastModified);
                    lastModified = getRandomDate(dateOfCreation);
                }
            }
        });
        return models;
    }

    private void createEAMContainer(List<Model> models) {
        List<OrganisationUnit> organisationUnits = new ArrayList<>();

        for (int i = 0; i < orgUnitNames.length; i++) {
            organisationUnits.add(new OrganisationUnit(orgUnitNames[i], i));
        }

        List<BusinessProcess> businessProcesses = new ArrayList<>();

        for (String businessProcessName : eamBusinessProcesses) {
            if (CollectionUtils.isEmpty(businessProcesses)) {
                businessProcesses.add(new BusinessProcess(null, businessProcessName));
            } else {
                businessProcesses.add(new BusinessProcess(
                        businessProcesses.get(businessProcesses.size() - 1), businessProcessName));
            }
        }

        List<EAMContainer> eamContainers = new ArrayList<>();
        models.stream()
                .filter(model -> model.getModelMetadata().getStatus().equals(ModelStatus.OPERATION) ||
                        model.getModelMetadata().getStatus().equals(ModelStatus.PLANNED) ||
                        model.getModelMetadata().getStatus().equals(ModelStatus.MAINTENANCE))
                .forEach(model -> {
                    EAMContainer eamContainer = EAMContainer.builder()
                            .businessProcess(getRandomElement(businessProcesses))
                            .organisationUnit(getRandomElement(organisationUnits))
                            .model(model).build();
                    eamContainers.add(eamContainer);
                });

        organisationUnitRepository.saveAll(organisationUnits);
        businessProcessRepository.saveAll(businessProcesses);
        eamContainerRepository.saveAll(eamContainers);
    }

    private void createModelFile(Iterable<Model> models) {
        models.forEach(model -> {
            if (model.getModelMetadata().getStatus() != ModelStatus.PLANNED) {
                try {
                    ModelFile modelFile = new ModelFile();
                    DBFile dbFile = new DBFile();
                    String fileName = getRandomElement(Arrays.asList(pmmlFiles));
                    dbFile.setFileName(fileName);
                    dbFile.setFileType("text/xml");
                    dbFile.setData(ByteStreams.toByteArray(getmPmmlInputstream(fileName)));
                    modelFile.setFileSize(dbFile.getData().length);
                    List<Model> modelList = pmmlMetadataParser.parsePMMLFile(getmPmmlInputstream(fileName), model.getModelMetadata());
                    model.setModelMetadata(modelList.get(0).getModelMetadata());
                    modelFile.setDbFile(dbFile);
                    modelFile.setModel(model);
                    dbFileRepository.save(dbFile);
                    modelFileRepository.save(modelFile);
                } catch (Exception e) {
                    log.info("Failed to load example pmml file, {}", e);
                }
            }
        });
    }

    private void createOpcuaFile(List<Model> models) {
        models.stream()
                .filter(model -> model.getRelationalDBInformation() == null)
                .forEach(model -> {
                    int n = ThreadLocalRandom.current().nextInt(4);
                    for (int i = 0; i < n; i++) {
                        OpcuaMetadata opcuaMetadata;
                        try {
                            String fileName = opcuaFiles[i];
                            DBFile dbFile = new DBFile();
                            dbFile.setFileName(fileName);
                            dbFile.setFileType("text/xml");
                            dbFile.setData(ByteStreams.toByteArray(getOpcuaInputstream(fileName)));
                            dbFile = dbFileRepository.save(dbFile);
                            opcuaMetadata = opcuaInformationModelService.parseOpcuaMetadata(getOpcuaInputstream(fileName), null, null);
                            OpcuaInformationModel opcuaInformationModel = new OpcuaInformationModel();
                            opcuaInformationModel.setDbFile(dbFile);
                            opcuaInformationModel.setOpcuaMetadata(opcuaMetadata);
                            opcuaInformationModel.setFileName(fileName);
                            opcuaInformationModel.setModel(model);
                            opcuaInformationModelRepository.save(opcuaInformationModel);
                        } catch (JAXBException | IOException e) {
                            log.info("Failed to parse opcua");
                        }
                    }
                });
    }

    private <T> T getRandomElement(List<T> list) {
        if (!list.isEmpty()) {
            int index = ThreadLocalRandom.current().nextInt(list.size());
            return list.get(index);
        } else {
            return null;
        }
    }

    private InputStream getOpcuaInputstream(String fileName) {
        try {
            return resourceLoader.getResource("classpath:/example/opcua/" + fileName).getInputStream();
        } catch (IOException e) {
            log.info("Failed to load file: {}", e);
        }
        return null;
    }

    private InputStream getmPmmlInputstream(String fileName) {
        try {
            return resourceLoader.getResource("classpath:/example/pmml/" + fileName).getInputStream();
        } catch (IOException e) {
            log.info("Failed to load file: {}", e);
        }
        return null;
    }

    private LocalDate getRandomDate(LocalDate dateMin) {
        int year = 365;
        return dateMin.plusDays((long) ThreadLocalRandom.current().nextInt(2 * year));
    }
}
