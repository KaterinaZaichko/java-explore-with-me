package ru.practicum.exception;

public class ForbiddenCommentException extends RuntimeException {
    public ForbiddenCommentException(String message) {
        super(message);
    }
}
