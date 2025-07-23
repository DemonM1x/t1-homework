package ru.t1.synthetichumancorestarter.errorhandling.exception;

public class QueueIsFullException extends Exception{
    public QueueIsFullException(String message, Exception e) {
        super(message, e);
    }
}
