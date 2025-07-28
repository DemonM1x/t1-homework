package ru.t1.synthetichumancorestarter.errorhandling.exception;

public class CommandExecutionException extends Exception{
    public CommandExecutionException(String message, Exception e) {
        super(message, e);
    }
}
