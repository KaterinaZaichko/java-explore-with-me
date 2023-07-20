package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.mapper.HitMapper;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Hit;
import ru.practicum.model.Stats;
import ru.practicum.repository.HitRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final HitRepository hitRepository;

    @Override
    public void save(HitDto hitDto) {
        Hit hit = HitMapper.toHit(hitDto);
        hitRepository.save(hit);
    }

    @Override
    public List<StatsDto> getStatsWithUris(String start, String end, String[] uris, boolean unique) {
        List<StatsDto> statsByRequest = new ArrayList<>();
        if (!(unique)) {
            if (hitRepository.findAllNonUniqueVisitsWithUris(start, end, uris) != null) {
                for (Stats stats : hitRepository.findAllNonUniqueVisitsWithUris(start, end, uris)) {
                    StatsDto statsDto = StatsMapper.toStatsDto(stats);
                    statsByRequest.add(statsDto);
                }
            }
        } else {
            if (hitRepository.findAllUniqueVisitsWithUris(start, end, uris) != null) {
                for (Stats stats : hitRepository.findAllUniqueVisitsWithUris(start, end, uris)) {
                    StatsDto statsDto = StatsMapper.toStatsDto(stats);
                    statsByRequest.add(statsDto);
                }
            }
        }
        return statsByRequest;
    }

    @Override
    public List<StatsDto> getStatsWithoutUris(String start, String end, boolean unique) {
        List<StatsDto> statsByRequest = new ArrayList<>();
        if (!(unique)) {
            if (hitRepository.findAllNonUniqueVisitsWithoutUris(start, end) != null) {
                for (Stats stats : hitRepository.findAllNonUniqueVisitsWithoutUris(start, end)) {
                    StatsDto statsDto = StatsMapper.toStatsDto(stats);
                    statsByRequest.add(statsDto);
                }
            }
        } else {
            if (hitRepository.findAllUniqueVisitsWithoutUris(start, end) != null) {
                for (Stats stats : hitRepository.findAllUniqueVisitsWithoutUris(start, end)) {
                    StatsDto statsDto = StatsMapper.toStatsDto(stats);
                    statsByRequest.add(statsDto);
                }
            }
        }
        return statsByRequest;
    }
}
