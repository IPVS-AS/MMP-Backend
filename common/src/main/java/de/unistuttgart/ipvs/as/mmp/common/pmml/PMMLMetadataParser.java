package de.unistuttgart.ipvs.as.mmp.common.pmml;

import de.unistuttgart.ipvs.as.mmp.common.domain.*;
import org.dmg.pmml.*;
import org.dmg.pmml.Interval;
import org.dmg.pmml.Model;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segment;
import org.dmg.pmml.mining.Segmentation;
import org.jpmml.model.PMMLUtil;
import org.jpmml.rexp.Converter;
import org.jpmml.rexp.ConverterFactory;
import org.jpmml.rexp.RExp;
import org.jpmml.rexp.RExpParser;
import org.jpmml.sklearn.ClassDictUtil;
import org.jpmml.sklearn.InputStreamStorage;
import org.jpmml.sklearn.PickleUtil;
import org.xml.sax.SAXException;
import sklearn.Estimator;
import sklearn.pipeline.Pipeline;
import sklearn2pmml.pipeline.PMMLPipeline;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Parser for the metadata of a PMML file.
 * This parser orientates on the general structure of a PMML file (not for a specific mining function/algorithm).
 *
 * @see <a href="http://dmg.org/pmml/v4-3/GeneralStructure.html"> general pmml structure</a>
 */
public class PMMLMetadataParser {

    private PMML load(InputStream is) throws SAXException, JAXBException {
        return PMMLUtil.unmarshal(is);
    }

    public List<de.unistuttgart.ipvs.as.mmp.common.domain.Model> parsePMMLFile(InputStream is, ModelMetadata modelMetadata) throws JAXBException, SAXException {
        PMML pmml;
        pmml = load(is);
        return parse(pmml, modelMetadata);
    }

    public List<de.unistuttgart.ipvs.as.mmp.common.domain.Model> parsePickleFile(InputStream is, ModelMetadata modelMetadata) throws IOException {
        InputStreamStorage storage = new InputStreamStorage(is);

        Object object = PickleUtil.unpickle(storage);
        if (!(object instanceof PMMLPipeline)) {

            // Create a single- or multi-step PMMLPipeline from a Pipeline
            if (object instanceof Pipeline) {
                Pipeline pipeline = (Pipeline) object;

                object = new PMMLPipeline()
                        .setSteps(pipeline.getSteps());
            } else {
                // Create a single-step PMMLPipeline from an Estimator
                if (object instanceof Estimator) {
                    Estimator estimator = (Estimator) object;

                    object = new PMMLPipeline()
                            .setSteps(Collections.singletonList(new Object[]{"estimator", estimator}));
                } else {
                    throw new IllegalArgumentException("The object (" + ClassDictUtil.formatClass(object) + ") is not a PMMLPipeline");
                }
            }
        }

        PMMLPipeline pipeline = (PMMLPipeline) object;

        PMML pmml;
        pmml = pipeline.encodePMML();
        return parse(pmml, modelMetadata);
    }

    public List<de.unistuttgart.ipvs.as.mmp.common.domain.Model> parseRFile(InputStream is, ModelMetadata
            modelMetadata) throws IOException {
        RExpParser parser = new RExpParser(is);
        RExp rexp = parser.parse();
        ConverterFactory converterFactory = ConverterFactory.newInstance();
        Converter<RExp> converter = converterFactory.newConverter(rexp);
        return parse(converter.encodePMML(), modelMetadata);
    }

    /**
     * Parses a PMML file from a given input stream.
     * The result should be a list of models with metadatas and each of the metadata should contain the parsed
     * informations from the PMML file.
     *
     * @param
     * @return List of models, where the metadata of each model contains the parsed information from the PMML file.
     */
    public List<de.unistuttgart.ipvs.as.mmp.common.domain.Model> parse(PMML pmml, ModelMetadata modelMetadata) {
        List<de.unistuttgart.ipvs.as.mmp.common.domain.Model> resultModels = new ArrayList<>();
        Header header = pmml.getHeader();

        if (modelMetadata == null) {
            modelMetadata = new ModelMetadata();
        }

        PMMLMetadata pmmlMetadata = new PMMLMetadata();

        //Todo this returns 4.3 even if the pmml Version is 3.2
        //reason for this is that 3.2 is also conform to 4.3
        String pmmlVersion = pmml.getVersion();
        if (!isNullOrEmpty(pmmlVersion)) {
            pmmlMetadata.setPmmlVersion(pmmlVersion);
        }

        //First set the metadata that is applicable to every model in the pmml file
        List<Annotation> annotations = header.getAnnotations();
        if (annotations != null && !annotations.isEmpty()) {
            List<String> stringAnnotations = parseAnnotations(annotations);
            pmmlMetadata.setPmmlAnnotations(stringAnnotations);
        }

        Timestamp timestamp = header.getTimestamp();
        if (timestamp != null) {
            List<Object> content = timestamp.getContent();
            if (content != null && !content.isEmpty()) {
                String timeStampValue = "";
                for (Object object : content) {
                    String value = object.toString();
                    if (!isNullOrEmpty(value)) {
                        timeStampValue += value;
                    }
                }
                pmmlMetadata.setTimestamp(timeStampValue);
            }
        }

        Application application = header.getApplication();
        if (application != null) {
            String frameWorkName = application.getName();
            if (!isNullOrEmpty(frameWorkName)) {
                pmmlMetadata.setApplicationName(frameWorkName);
            }

            String applicationVersion = application.getVersion();
            if (!isNullOrEmpty(applicationVersion)) {
                pmmlMetadata.setApplicationVersion(applicationVersion);
            }
        }

        String modelDescription = header.getDescription();
        if (!isNullOrEmpty(modelDescription)) {
            pmmlMetadata.setDescription(modelDescription);

            if (isNullOrEmpty(modelMetadata.getModelDescription())) {
                modelMetadata.setModelDescription(modelDescription);
            }
        }

        String modelVersion = header.getModelVersion();
        if (!isNullOrEmpty(modelVersion)) {
            pmmlMetadata.setModelVersion(modelVersion);
        }

        //Contains all the information about the training run
        //but only as extensions and extensions can contain everything ...
        MiningBuildTask miningBuildTask = pmml.getMiningBuildTask();
        TrainingRun trainingRun = parseBuildingTaskToTrainingRun(miningBuildTask);

        //Data dictionary contains a dictionary with the all attributes and their types
        //But not all of them are used, the ones used for the model stand in MiningField
        //But to know the type of attribute of the mining field, the map attributeNameToType is needed
        DataDictionary dataDictionary = pmml.getDataDictionary();

        // The training run attributes and values were set independent of the model
        // Also only one training run is considered ...
        //Maybe remove it? Couldn't find a pmml file where it was used ...
        if (trainingRun != null) {
            modelMetadata.setTrainingRuns(Collections.singletonList(trainingRun));
        }

        if (pmml.hasModels()) {
            //A pmml file could contain more than one model
            List<Model> models = pmml.getModels();
            for (Model model : models) {
                de.unistuttgart.ipvs.as.mmp.common.domain.Model resultModel
                        = new de.unistuttgart.ipvs.as.mmp.common.domain.Model();

                if (model instanceof MiningModel) {
                    MiningModel miningModel = (MiningModel) model;
                    Segmentation segmentation = miningModel.getSegmentation();
                    //if there is a segmentation, then this means there is one model element but multiple models
                    //are defined in the segmenation element, so each segment has one model
                    if (segmentation != null) {
                        List<Segment> segments = segmentation.getSegments();
                        for (Segment segment : segments) {
                            ModelMetadata segmentationModelMetadata = modelMetadata;
                            Model segmentModel = segment.getModel();

                            segmentationModelMetadata = parseModelToMetadata(segmentModel, segmentationModelMetadata, dataDictionary, pmmlMetadata);
                            resultModel.setModelMetadata(segmentationModelMetadata);
                            resultModels.add(resultModel);
                        }
                    }
                } else {
                    ModelMetadata singleMetadata = modelMetadata;

                    singleMetadata = parseModelToMetadata(model, singleMetadata, dataDictionary, pmmlMetadata);
                    resultModel.setModelMetadata(singleMetadata);
                    resultModels.add(resultModel);
                }
            }
        }
        return resultModels;
    }

    private ModelMetadata parseModelToMetadata(Model model, ModelMetadata metadata, DataDictionary dataDictionary, PMMLMetadata pmmlMetadata) {

        String modelName = model.getModelName();
        if (!isNullOrEmpty(modelName) && isNullOrEmpty(metadata.getName())) {
            metadata.setName(modelName);
        }

        //mining function is the general function (classification, clustering, trees, ...)
        MiningFunction miningFunction = model.getMiningFunction();
        if (miningFunction != null) {
            String value = miningFunction.value();
            if (!isNullOrEmpty(value)) {
                pmmlMetadata.setMiningFunction(value);
            }
        }

        //describes specific name of the algorithm
        String algorithmName = model.getAlgorithmName();
        if (!isNullOrEmpty(algorithmName)) {
            pmmlMetadata.setAlgorithmName(algorithmName);

            if (isNullOrEmpty(metadata.getAlgorithm())) {
                metadata.setAlgorithm(algorithmName);
            }
        }

        //parse the local transformations, these are the preprocessing steps that were done before applying
        //the algorithm. For each attribute there could an other transformation, so the transformation also
        //needs the attribute.
        LocalTransformations localTransformations = model.getLocalTransformations();
        if (localTransformations != null) {
            List<Transformation> transformations = parseLocalTransformations(localTransformations);
            transformations.forEach(transformation -> transformation.setFromFramework(pmmlMetadata.getApplicationName()));
            metadata.setTransformations(transformations);
        }

        // mining fields are the fields (attributes) that were used for the data mining algorithm
        MiningSchema miningSchema = model.getMiningSchema();
        List<MiningField> miningFields = miningSchema.getMiningFields();
        List<InputAttribute> inputAttributes = parseMiningFieldsToAttributes(miningFields, dataDictionary);

        Output output = model.getOutput();
        if (output != null) {
            List<OutputAttribute> outputAttributes = parseOutputFields(output);
            pmmlMetadata.setOutputAttributes(outputAttributes);
        }
        pmmlMetadata.setInputAttributes(inputAttributes);
        metadata.setPmmlMetadata(pmmlMetadata);
        return metadata;
    }

    private List<OutputAttribute> parseOutputFields(Output output) {
        List<OutputAttribute> outputAttributes = new ArrayList<>();
        List<OutputField> outputFields = output.getOutputFields();
        for (OutputField outputField : outputFields) {
            OutputAttribute attribute = new OutputAttribute();
            FieldName fieldName = outputField.getName();
            if (fieldName != null) {
                String name = fieldName.getValue();
                if (!isNullOrEmpty(name)) {
                    attribute.setName(name);
                }
            }
            ResultFeature resultFeature = outputField.getResultFeature();
            if (resultFeature != null) {
                String value = resultFeature.value();
                attribute.setResultFeature(value);
            }

            OpType opType = outputField.getOpType();
            if (opType != null) {
                String opTypeValue = opType.value();
                if (!isNullOrEmpty(opTypeValue)) {
                    attribute.setOpType(opTypeValue);
                }
            }
            outputAttributes.add(attribute);
        }
        return outputAttributes;
    }

    /**
     * Parses the mining fields from the pmml file and maps them to attributes.
     * Therefore the values fieldname, fieldkey (should be unique), how to handle missing values (missingValueReplacement),
     * invalid values and the usage type are considered.
     *
     * @param miningFields
     * @param dataDictionary
     * @return List of attributes that were created from the mining field. One attribute per mining filed should be created.
     */
    private List<InputAttribute> parseMiningFieldsToAttributes(List<MiningField> miningFields, DataDictionary dataDictionary) {
        List<InputAttribute> attributes = new ArrayList<>();
        List<DataField> dataFields = dataDictionary.getDataFields();
        for (MiningField miningField : miningFields) {
            InputAttribute attribute = new InputAttribute();
            String fieldName = miningField.getName().getValue();
            DataField correspondingDataField = findDataFieldWithFieldName(fieldName, dataFields);

            //Find the corresponding data field to the miningField from the dictionary
            if (!isNullOrEmpty(fieldName)) {
                attribute.setName(fieldName);
                String dataType = correspondingDataField.getDataType().value();
                if (!isNullOrEmpty(dataType)) {
                    attribute.setDataType(dataType);
                }
            }

            if (isOpTypeCategoricalOrOrdinal(correspondingDataField.getOpType())) {
                List<Value> values = correspondingDataField.getValues();
                if (!isListNullOrEmpty(values)) {
                    List<String> valuesAsStrings = values.stream().map(Value::getValue).collect(Collectors.toList());
                    attribute.setPossibleValues(valuesAsStrings);
                }
            } else {
                //if type is not categorical or ordinal it has to be continuous
                //this does means that there could be intervals for the values
                List<Interval> intervals = correspondingDataField.getIntervals();
                if (!isListNullOrEmpty(intervals)) {
                    List<de.unistuttgart.ipvs.as.mmp.common.domain.Interval> valueIntervals = new ArrayList<>();
                    for (Interval interval : intervals) {
                        de.unistuttgart.ipvs.as.mmp.common.domain.Interval valueInterval = new de.unistuttgart.ipvs.as.mmp.common.domain.Interval();

                        Interval.Closure closure = interval.getClosure();
                        if (closure != null) {
                            String closureValue = closure.value();
                            if (!isNullOrEmpty(closureValue)) {
                                valueInterval.setClosure(closureValue);
                            }
                        }

                        Double leftMargin = interval.getLeftMargin();
                        if (leftMargin != null) {
                            valueInterval.setStartRange(leftMargin);
                        }

                        Double rightMargin = interval.getRightMargin();
                        if (rightMargin != null) {
                            valueInterval.setEndRange(rightMargin);
                        }

                        valueIntervals.add(valueInterval);
                    }
                    attribute.setIntervals(valueIntervals);
                }
            }

            String missingValueReplacement = miningField.getMissingValueReplacement();
            if (!isNullOrEmpty(missingValueReplacement)) {
                attribute.setMissingValueReplacement(missingValueReplacement);
            }

            String invalidValueReplacement = miningField.getInvalidValueReplacement();
            if (!isNullOrEmpty(invalidValueReplacement)) {
                attribute.setInvalidValueReplacement(invalidValueReplacement);
            }

            MiningField.UsageType usageType = miningField.getUsageType();
            if (usageType != null) {
                if (usageType.equals(MiningField.UsageType.PREDICTED)
                        || usageType.equals(MiningField.UsageType.TARGET)) {
                    //Do not add this attribute here since it is only used for prediction so not as input
                    continue;
                }
                String usageTypeValue = usageType.value();
                if (!isNullOrEmpty(usageTypeValue)) {
                    attribute.setUsageType(usageTypeValue);
                }
            }

            attributes.add(attribute);
        }
        return attributes;
    }

    private boolean isListNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }

    private boolean isOpTypeCategoricalOrOrdinal(OpType opType) {
        return opType.equals(OpType.CATEGORICAL) || opType.equals(OpType.ORDINAL);
    }

    private DataField findDataFieldWithFieldName(String fieldName, List<DataField> dataFields) {
        for (DataField dataField : dataFields) {
            if (fieldName.equals(dataField.getName().getValue())) {
                return dataField;
            }
        }
        return null;
    }

    /**
     * Parses the local transformations. The local transformations are like preprocessing steps that were performed on
     * the mining fields before the algorihtm were executed. So the transformations are based on the mining fields.
     * A transformation in PMML is a expression and the mining field that was changed is the derivedField. Therefore each
     * of the transformations has a attribute on which the transformation was done.
     *
     * @param localTransformations
     * @return List of transformations that were done on the mining fields.
     */
    private List<Transformation> parseLocalTransformations(LocalTransformations localTransformations) {
        List<Transformation> transformations = new ArrayList<>();
        if (localTransformations.hasDerivedFields()) {
            List<DerivedField> derivedFields = localTransformations.getDerivedFields();
            for (DerivedField derivedField : derivedFields) {
                InputAttribute attribute = new InputAttribute();
                Transformation transformation = new Transformation();
                String derivedFieldDisplayName = derivedField.getDisplayName();
                if (!isNullOrEmpty(derivedFieldDisplayName)) {
                    attribute.setName(derivedFieldDisplayName);
                }

                transformation.setAttributeToTransform(attribute);
                Expression expression = derivedField.getExpression();
                transformation.setName(expression.toString());
                transformations.add(transformation);
            }
        }
        return transformations;
    }

    /**
     * Parses the miningBuildTask to a training run. The miningBuildTask contains all information that were done
     * on the training run.
     *
     * @param miningBuildTask
     * @return Training run that was described by the miningBuildTask
     */
    private TrainingRun parseBuildingTaskToTrainingRun(MiningBuildTask miningBuildTask) {
        TrainingRun trainingRun = null;
        if (miningBuildTask != null) {
            HashMap<String, String> trainingAttributeToValue = new HashMap<>();
            //Unfortunately the miningBuildTask only has extensions, which could be any field ...
            //so we are saving the attributes with their values in a map and giving them to the training run
            miningBuildTask.getExtensions().forEach(e -> {
                if (!isNullOrEmpty(e.getName()) && !isNullOrEmpty(e.getValue())) {
                    trainingAttributeToValue.put(e.getName(), e.getValue());
                }
            });
            trainingRun = new TrainingRun();
            trainingRun.setAttributeToValue(trainingAttributeToValue);
        }
        return trainingRun;
    }

    private List<String> parseAnnotations(List<Annotation> annotations) {
        List<String> stringAnnotations = new ArrayList<>();
        for (Annotation annotation : annotations) {
            List<Object> content = annotation.getContent();
            for (Object value : content) {
                String stringValue = String.valueOf(value);
                if (!isNullOrEmpty(stringValue)) {
                    stringAnnotations.add(stringValue);
                }
            }
        }
        return stringAnnotations;
    }

    private boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
