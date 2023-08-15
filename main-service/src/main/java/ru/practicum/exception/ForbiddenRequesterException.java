package ru.practicum.exception;

public class ForbiddenRequesterException extends RuntimeException {
    public ForbiddenRequesterException(String message) {
        super(message);
    }
}
