package ru.practicum.controller.closed;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@AllArgsConstructor
@Slf4j
@Validated
public class ClosedEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@PathVariable long userId,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Getting events");
        return eventService.getEventsByUserId(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto saveEvent(@PathVariable long userId,
                                  @RequestBody @Valid NewEventDto newEventDto) {
        log.info("Creating event");
        return eventService.save(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByUserIdAndEventId(@PathVariable long userId,
                                                   @PathVariable long eventId) {
        log.info("Getting event");
        return eventService.getEventByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable long userId,
                                    @PathVariable long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("Updating event");
        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByUserIdAndEventId(@PathVariable long userId,
                                                                       @PathVariable long eventId) {
        log.info("Getting requests");
        return eventService.getRequestsByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequests(@PathVariable long userId,
                                                         @PathVariable long eventId,
                                                         @RequestBody @Valid
                                                         EventRequestStatusUpdateRequest
                                                                 eventRequestStatusUpdateRequest) {
        log.info("Updating request");
        return eventService.updateRequests(userId, eventId, eventRequestStatusUpdateRequest);
    }
}
