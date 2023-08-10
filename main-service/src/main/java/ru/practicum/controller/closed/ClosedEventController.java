package ru.practicum.controller.closed;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.service.EventService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@AllArgsConstructor
@Slf4j
public class ClosedEventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(@PathVariable long userId,
                                                         @RequestParam(defaultValue = "0") int from,
                                                         @RequestParam(defaultValue = "10") int size) {
        log.info("Getting events");
        return new ResponseEntity<>(eventService.getEventsByUserId(userId, from, size), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EventFullDto> saveEvent(@PathVariable long userId,
                                                  @RequestBody @Valid NewEventDto newEventDto) {
        log.info("Creating event");
        return new ResponseEntity<>(eventService.save(userId, newEventDto), HttpStatus.CREATED);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getEventByUserIdAndEventId(@PathVariable long userId,
                                                                   @PathVariable long eventId) {
        log.info("Getting event");
        return new ResponseEntity<>(eventService.getEventByUserIdAndEventId(userId, eventId), HttpStatus.OK);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable long userId,
                                                    @PathVariable long eventId,
                                                    @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("Updating event");
        return new ResponseEntity<>(eventService.updateEvent(userId, eventId, updateEventUserRequest), HttpStatus.OK);
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsByUserIdAndEventId(@PathVariable long userId,
                                                                                       @PathVariable long eventId) {
        log.info("Getting requests");
        return new ResponseEntity<>(eventService.getRequestsByUserIdAndEventId(userId, eventId), HttpStatus.OK);
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequests(@PathVariable long userId,
                                                                         @PathVariable long eventId,
                                                                         @RequestBody @Valid EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Updating request");
        return new ResponseEntity<>(eventService.updateRequests(userId, eventId, eventRequestStatusUpdateRequest),
                HttpStatus.OK);
    }
}
