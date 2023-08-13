package ru.practicum.dto.event;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class LocationDto {
    @NotNull
    private float lat;
    @NotNull
    private float lon;
}
