package ru.practicum.model;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
    private String[] errors;
    private String message;
    private String reason;
    private HttpStatus status;
    private LocalDateTime timestamp;
}
