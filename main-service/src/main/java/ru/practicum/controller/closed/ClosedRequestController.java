package ru.practicum.controller.closed;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<ParticipationRequestDto>> getRequests(@PathVariable long userId) {
        log.info("Getting requests");
        return new ResponseEntity<>(requestService.getAll(userId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> saveRequest(@PathVariable long userId,
                                                               @RequestParam long eventId) {
        log.info("Creating request");
        return new ResponseEntity<>(requestService.save(userId, eventId), HttpStatus.CREATED);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> updateRequest(@PathVariable long userId,
                                                                 @PathVariable long requestId) {
        log.info("Updating request");
        return new ResponseEntity<>(requestService.update(userId, requestId), HttpStatus.OK);
    }
}
