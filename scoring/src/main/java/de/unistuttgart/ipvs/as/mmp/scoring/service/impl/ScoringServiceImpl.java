package de.unistuttgart.ipvs.as.mmp.scoring.service.impl;

import de.unistuttgart.ipvs.as.mmp.common.domain.DBFile;
import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.Scoring;
import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.ScoringInput;
import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.ScoringOutput;
import de.unistuttgart.ipvs.as.mmp.common.exception.*;
import de.unistuttgart.ipvs.as.mmp.model.service.ModelService;
import de.unistuttgart.ipvs.as.mmp.scoring.repository.ScoringRepository;
import de.unistuttgart.ipvs.as.mmp.scoring.service.ScoringService;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;
import org.jpmml.model.VisitorBattery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ScoringServiceImpl implements ScoringService {

    private final ModelService modelService;
    private final ScoringRepository scoringRepository;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ScoringServiceImpl(ModelService modelService, ScoringRepository scoringRepository) {
        this.modelService = modelService;
        this.scoringRepository = scoringRepository;
    }

    @Override
    public List<ScoringOutput> scoreModel(Long projectId, Long modelId, List<ScoringInput> inputs) {
        DBFile pmmlFile;
        Optional<Model> optModel = this.modelService.getModelForProjectById(projectId, modelId);

        if (!optModel.isPresent()) {
            this.log.error("Optional of model is not present");
            throw IdException.idNotFound(Model.class, modelId);
        }

        Model model = optModel.get();
        pmmlFile = model.getModelFile().getDbFile();
        PMML pmml;

        try (InputStream is = new ByteArrayInputStream(pmmlFile.getData())) {
            pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
        } catch (IOException | SAXException | JAXBException e) {
            this.log.error(e.getMessage(), e.getCause());
            throw ScoringException.parsingError();
        }

        Evaluator evaluator = getEvaluator(pmml);
        try {
            evaluator.verify();
        } catch (Exception e) {
            this.log.error(e.getMessage(), e.getCause());
            throw ScoringException.verifyError();
        }

        Map<FieldName, FieldValue> arguments = getArguments(evaluator, inputs);

        Map<FieldName, ?> results;
        try {
            results = evaluator.evaluate(arguments);
        } catch (Exception e) {
            this.log.error(e.getMessage(), e.getCause());
            throw ScoringException.evaluateError();
        }

        List<ScoringOutput> scoringOutputs = new ArrayList<>();
        List<OutputField> outputFields = evaluator.getOutputFields();
        if (!outputFields.isEmpty()) {
            for (OutputField outputField : outputFields) {
                FieldName outputFieldName = outputField.getName();
                Object outputFieldValue = results.get(outputFieldName);
                scoringOutputs.add(new ScoringOutput(outputFieldName.toString(), outputFieldValue.toString()));
            }
        } else {
            List<TargetField> targetFields = evaluator.getTargetFields();
            for (TargetField targetField : targetFields) {
                FieldName targetFieldName = targetField.getName();
                Object targetFieldValue = results.get(targetFieldName);
                scoringOutputs.add(new ScoringOutput(targetFieldName.toString(), targetFieldValue.toString()));
            }
        }

        if(scoringOutputs.isEmpty()) {
            this.log.error("No output could be generated.");
            throw ScoringException.outputError();
        }

        Scoring scoring = Scoring.builder().model(model).inputs(inputs).outputs(scoringOutputs).build();
        scoringRepository.save(scoring);

        if (model.getScorings() == null) {
            model.setScorings(new ArrayList<>());
        }
        model.getScorings().add(scoring);

        return scoringOutputs;
    }

    private Evaluator getEvaluator(PMML pmml) {
        VisitorBattery visitorBattery = new VisitorBattery();
        visitorBattery.add(org.jpmml.model.visitors.LocatorNullifier.class);
        visitorBattery.addAll(new org.jpmml.model.visitors.AttributeInternerBattery());
        visitorBattery.applyTo(pmml);

        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        ValueFactoryFactory valueFactoryFactory = ReportingValueFactoryFactory.newInstance();
        modelEvaluatorFactory.setValueFactoryFactory(valueFactoryFactory);

        return modelEvaluatorFactory.newModelEvaluator(pmml);
    }

    private Map<FieldName, FieldValue> getArguments(Evaluator evaluator, List<ScoringInput> inputs) {
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
        List<InputField> inputFields = evaluator.getActiveFields();

        for (InputField inputField : inputFields) {
            FieldName inputFieldName = inputField.getName();
            for (ScoringInput scoringInput : inputs) {
                if (inputFieldName.toString().equalsIgnoreCase(scoringInput.getName())) {
                    Object rawValue = scoringInput.getValue();
                    FieldValue inputFieldValue = inputField.prepare(rawValue);
                    arguments.put(inputFieldName, inputFieldValue);
                    break;
                }
            }
        }

        return arguments;
    }
}
