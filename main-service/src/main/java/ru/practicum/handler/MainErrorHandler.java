package ru.practicum.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.*;
import ru.practicum.model.ApiError;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class MainErrorHandler {
    @ExceptionHandler({NameUniquenessViolationException.class,
            CategoryVoidViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflictHandler(final RuntimeException e) {
        log.info("409: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({UserNotFoundException.class,
            CategoryNotFoundException.class,
            EventNotFoundException.class,
            ParticipationRequestNotFoundException.class,
            CompilationNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFoundHandler(final RuntimeException e) {
        log.info("404: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("The required object was not found.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({ForbiddenDateException.class,
            ForbiddenStateException.class,
            ForbiddenRequesterException.class,
            ForbiddenParticipantsCountException.class,
            ForbiddenRequestException.class,
            HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError forbiddenHandler(final RuntimeException e) {
        log.info("409: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.FORBIDDEN)
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequestHandler(final RuntimeException e) {
        log.info("400: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
