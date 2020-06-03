package de.unistuttgart.ipvs.as.mmp.common.exception;

public class ScoringException extends RuntimeException {

    private ScoringException(String message) {
        super(message);
    }

    public static ScoringException parsingError() {
        return new ScoringException("Could not unmarshal the model out of the pmml file. Please review your model file.");
    }

    public static ScoringException verifyError() {
        return new ScoringException("The pmml model could not be verified. Please review your model.");
    }

    public static ScoringException evaluateError() {
        return new ScoringException("Could not evaluate the model with the specified inputs. Please review your inputs and try again.");
    }

    public static ScoringException outputError() {
        return new ScoringException("No output could be generated. Please check your model and declare output or target fields.");
    }
}