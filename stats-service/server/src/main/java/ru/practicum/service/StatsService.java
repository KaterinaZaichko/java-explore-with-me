package ru.practicum.service;

import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void save(HitDto hitDto);

    List<StatsDto> getStatsWithUris(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique);

    List<StatsDto> getStatsWithoutUris(LocalDateTime start, LocalDateTime end, boolean unique);
}
