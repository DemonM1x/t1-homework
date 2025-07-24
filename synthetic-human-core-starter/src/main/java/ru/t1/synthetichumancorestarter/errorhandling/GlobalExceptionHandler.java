package ru.t1.synthetichumancorestarter.errorhandling;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.t1.synthetichumancorestarter.errorhandling.exception.AuditException;
import ru.t1.synthetichumancorestarter.errorhandling.exception.CommandExecutionException;
import ru.t1.synthetichumancorestarter.errorhandling.exception.QueueIsFullException;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleValidationException(ConstraintViolationException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, e, "Invalid command fields");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleArgumentException(IllegalArgumentException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, e, "Invalid command arguments");
    }

    @ExceptionHandler(QueueIsFullException.class)
    public ErrorResponse handleQueueIsFullException(QueueIsFullException e) {
        return createErrorResponse(HttpStatus.TOO_MANY_REQUESTS, e, "Android can't execute too many commands. Queue is full");
    }

    @ExceptionHandler(CommandExecutionException.class)
    public ErrorResponse handleCommandExecutionException(CommandExecutionException e) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, "Command execution failed");
    }

    @ExceptionHandler(AuditException.class)
    public ErrorResponse handleAuditException(AuditException e) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, "Audit failed. Error sending audit data");
    }


    private ErrorResponse createErrorResponse(HttpStatus code, Exception ex, String extraDetails) {
        logError(ex, code);

        String details = ex.getMessage();
        if (ex.getCause() != null) {
            details += "Caused by: " + ex.getCause().getMessage();
        }
        return ErrorResponse.builder(ex, code, details)
                .detail(extraDetails)
                .type(URI.create("/%s/%s".formatted("android", "error")))
                .property("Error class", ex.getClass())
                .property("timestamp", Instant.now())
                .build();
    }

    private void logError(Exception ex, HttpStatus code) {
        if (code.is5xxServerError()) {
            log.error("Internal error", ex);
        } else {
            log.debug("User error", ex);
        }
    }


}
