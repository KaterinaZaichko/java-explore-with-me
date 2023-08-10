package ru.practicum.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public ResponseEntity<Object> saveHit(@RequestBody @Valid HitDto hitDto) {
        log.info("Creating hit");
        statsService.save(hitDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public List<StatsDto> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                   LocalDateTime start,
                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                   LocalDateTime end,
                                   @RequestParam(required = false) String[] uris,
                                   @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Getting stats");
        if (uris == null) {
            return statsService.getStatsWithoutUris(start, end, unique);
        }
        return statsService.getStatsWithUris(start, end, uris, unique);
    }
}
