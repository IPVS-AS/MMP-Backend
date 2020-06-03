package de.unistuttgart.ipvs.as.mmp.common.util;

import de.unistuttgart.ipvs.as.mmp.common.domain.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

public class DefaultDataBuilder {

    private DefaultDataBuilder() {
    }

    public static final Hyperparameter.HyperparameterBuilder hyperparamterBuilder = Hyperparameter.builder().name("minNumObj").value("2");

    public static final TrainingRun.TrainingRunBuilder trainingRunBuilder = TrainingRun.builder()
            .startTime(LocalDate.now())
            .endTime(LocalDate.now().plusDays(1))
            .annotations(Collections.singletonList("First Training"));

    public static final Dataset.DatasetBuilder dataSetBuilder = Dataset.builder().type("csv").source("/home/ubuntu/test.csv").version("1.1")
            .annotations(Collections.singletonList("Dataset for Predictive Maintenance"));

    public static final Score.ScoreBuilder scoreBuilder = Score.builder().metricName("F1-Score")
            .value(0.86);

    public static final Evaluation.EvaluationBuilder evaluationBuilder = Evaluation.builder().name("Evaluation of Machine XYZ Data");

    public static final PredictionMetadata.PredictionMetadataBuilder predictionMetadataBuilder = PredictionMetadata.builder()
            .annotation("Final Evaluation of the Model");


    public static final CustomField.CustomFieldBuilder customFieldBuilder = CustomField.builder().fieldName("Python Version")
            .fieldContent("3.6");



    public static final User.UserBuilder userBuilder =
            User.builder()
                    .name("Max Mustermann")
                    .role("admin")
                    .password("passwordMustermann");


    public static final Transformation.TransformationBuilder transformationBuilder =
            Transformation.builder().name("Remove null values")
                    .parameters(Collections.singletonList("column2"))
                    .input(dataSetBuilder.build())
                    .output(dataSetBuilder.build());
    public static final ModelGroup.ModelGroupBuilder modelGroupBuilder = ModelGroup.builder().modelGroupName("test group");


    public static final ModelMetadata.ModelMetadataBuilder modelMetadataBuilder = ModelMetadata.builder().name("Best Model").status(ModelStatus.OPERATION)
            .lastModified(LocalDate.now())
            .dateOfCreation(LocalDate.now()).algorithm("J48").customFields(Collections.singletonList(customFieldBuilder.build()))
            .trainingRuns(Collections.singletonList(trainingRunBuilder.currentScore(scoreBuilder.value(85.6).build()).build()))
            .hyperparameters(Arrays.asList(hyperparamterBuilder.build(), hyperparamterBuilder.value("0,25").build()))
            .transformations(Collections.singletonList(getNewTransformation()))
            .predictionMetadata(predictionMetadataBuilder.evaluation(evaluationBuilder.scores(Collections.singletonList(scoreBuilder.build())).build()).build())
            .author(userBuilder.build());

    public static ModelMetadata.ModelMetadataBuilder getNewMetadataBuilder() {
        return modelMetadataBuilder.customFields(Collections.singletonList(customFieldBuilder.build()))
                .trainingRuns(Collections.singletonList(trainingRunBuilder.currentScore(scoreBuilder.value(85.6).build()).build()))
                .hyperparameters(Arrays.asList(hyperparamterBuilder.build(), hyperparamterBuilder.value("0,25").build()))
                .transformations(Collections.singletonList(getNewTransformation()))
                .modelDescription("Best Model")
                .modelGroup(modelGroupBuilder.build())
                .predictionMetadata(predictionMetadataBuilder.evaluation(evaluationBuilder.scores(Collections.singletonList(scoreBuilder.build())).build()).build());
    }

    private static Transformation getNewTransformation() {
        return transformationBuilder
                .input(dataSetBuilder.build())
                .output(dataSetBuilder.build()).build();
    }


    public static final Project.ProjectBuilder projetBuilder =
            Project.builder()
                    .name("EnPro Project")
                    .description("This is a description of the EnPro Project")
                    .creationDate(LocalDate.now())
                    .startDate(LocalDate.of(2018, 4, 1))
                    .endDate(LocalDate.of(2018, 10, 1))
                    .status(ProjectStatus.NEW)
                    .useCase("lead time improvement")
                    .editors(Collections.singletonList(userBuilder.build()));

    public static final Model.ModelBuilder modelBuilder =
            Model.builder()
                    .modelMetadata(getNewMetadataBuilder().build())
                    .project(projetBuilder.build());
}
