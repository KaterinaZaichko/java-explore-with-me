package ru.practicum.controller.closed;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@AllArgsConstructor
@Slf4j
public class ClosedRequestController {
    private final ParticipationRequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getRequests(@PathVariable long userId) {
        log.info("Getting requests");
        return requestService.getAll(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto saveRequest(@PathVariable long userId,
                                               @RequestParam long eventId) {
        log.info("Creating request");
        return requestService.save(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto updateRequest(@PathVariable long userId,
                                                 @PathVariable long requestId) {
        log.info("Updating request");
        return requestService.update(userId, requestId);
    }
}
