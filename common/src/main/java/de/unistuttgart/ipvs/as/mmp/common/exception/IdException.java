package de.unistuttgart.ipvs.as.mmp.common.exception;

public class IdException extends RuntimeException {

    private IdException(String message) {
        super(message);
    }

    public static IdException idNotFound(Class clazz, Long id) {
        return new IdException(String.format("Object of type %s not found for id %s", clazz.getTypeName(), id));
    }

}