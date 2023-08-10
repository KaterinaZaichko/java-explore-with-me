package ru.practicum.exception;

public class ForbiddenParticipantsCountException extends RuntimeException {
    public ForbiddenParticipantsCountException(String message) {
        super(message);
    }
}
