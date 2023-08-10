package ru.practicum.exception;

public class ForbiddenStateException extends RuntimeException {
    public ForbiddenStateException(String message) {
        super(message);
    }
}
