package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.DateTimeConstant;
import ru.practicum.dto.HitDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;

@UtilityClass
public class HitMapper {
    public Hit toHit(HitDto hitDto) {
        return Hit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(LocalDateTime.parse(hitDto.getTimestamp(), DateTimeConstant.dtFormatter))
                .build();
    }
}
