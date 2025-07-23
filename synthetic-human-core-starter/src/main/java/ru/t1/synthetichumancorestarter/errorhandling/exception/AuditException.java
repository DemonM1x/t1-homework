package ru.t1.synthetichumancorestarter.errorhandling.exception;

public class AuditException extends Exception{
    public AuditException(final String message, Exception e) {
        super(message, e);
    }
}
