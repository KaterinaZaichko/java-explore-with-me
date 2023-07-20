package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Builder
public class HitDto {
    @NotNull
    private String app;
    private String uri;
    @NotNull
    private String ip;
    @NotNull
    private String timestamp;
}
