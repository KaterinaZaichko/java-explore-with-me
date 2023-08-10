package ru.practicum.exception;

public class NameUniquenessViolationException extends RuntimeException {
    public NameUniquenessViolationException(String message) {
        super(message);
    }
}
