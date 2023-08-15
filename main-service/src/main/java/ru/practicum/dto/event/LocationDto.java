package ru.practicum.dto.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationDto {
    private float lat;
    private float lon;
}
