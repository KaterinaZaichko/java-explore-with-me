package ru.practicum.exception;

public class ForbiddenDateException extends RuntimeException {
    public ForbiddenDateException(String message) {
        super(message);
    }
}
