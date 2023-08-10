package ru.practicum.controller.admin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.service.EventService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@AllArgsConstructor
@Slf4j
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getEvents(@RequestParam(required = false) long[] users,
                                                        @RequestParam(required = false) String[] states,
                                                        @RequestParam(required = false) long[] categories,
                                                        @RequestParam(required = false)
                                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                        LocalDateTime rangeStart,
                                                        @RequestParam(required = false)
                                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                        LocalDateTime rangeEnd,
                                                        @RequestParam(defaultValue = "0") int from,
                                                        @RequestParam(defaultValue = "10") int size) {
        log.info("Getting events");
        return new ResponseEntity<>(eventService.getEventsByUsersAndStatesAndCategoriesAndRanges(
                users, states, categories, rangeStart, rangeEnd, from, size),
                HttpStatus.OK);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable long eventId,
                                                    @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Updating event");
        return new ResponseEntity<>(eventService.update(eventId, updateEventAdminRequest), HttpStatus.OK);
    }
}
