package de.unistuttgart.ipvs.as.mmp.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
    public class MMPExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(value = IdException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ResponseBody
    public String idError(IdException idException) {
        log.info(idException.getMessage());
        return idException.getLocalizedMessage();
    }

    @ExceptionHandler(value = ScoringException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String scoringError(ScoringException scoringException) {
        log.info(scoringException.getMessage());
        return scoringException.getLocalizedMessage();
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String argumentError(IllegalArgumentException argumentException) {
        log.info(argumentException.getMessage());
        return argumentException.getLocalizedMessage();
    }

    @ExceptionHandler(value = RuntimeException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String internalError(RuntimeException runtimeError) {
        log.warn(runtimeError.getMessage());
        return "Something went wrong! If this problem persists please talk to your system administrator.";
    }

}
