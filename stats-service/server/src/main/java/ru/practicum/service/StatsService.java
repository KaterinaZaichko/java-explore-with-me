package ru.practicum.service;

import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;

import java.util.List;

public interface StatsService {
    void save(HitDto hitDto);

    List<StatsDto> getStatsWithUris(String start, String end, String[] uris, boolean unique);

    List<StatsDto> getStatsWithoutUris(String start, String end, boolean unique);
}
