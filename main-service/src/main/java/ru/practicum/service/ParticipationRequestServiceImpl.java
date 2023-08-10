package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.exception.*;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository participationRequestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequestDto> getAll(long userId) {
        User requester =  userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User with id=%d was not found", userId)));
        List<ParticipationRequestDto> participationRequests = new ArrayList<>();
        for (ParticipationRequest participationRequest : participationRequestRepository.findAllByRequester(requester)) {
            participationRequests.add(ParticipationRequestMapper.toParticipationRequestDto(participationRequest));
        }
        return participationRequests;
    }

    @Override
    public ParticipationRequestDto save(long userId, long eventId) {
        User requester = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User with id=%d was not found", userId)));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(
                String.format("Event with id=%d was not found", eventId)));
        if (participationRequestRepository.findByRequesterAndEvent(requester, event) != null) {
            throw new ForbiddenRequestException("Can't add a repeat request");
        }
        if (event.getInitiator().equals(requester)) {
            throw new ForbiddenRequesterException("The initiator of the event cannot add a request to participate " +
                    "in his event");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ForbiddenStateException("You can't participate in an unpublished event");
        }
        if (event.getParticipantLimit() != 0
                && event.getParticipantLimit() == participationRequestRepository.findAllByEvent(event).size()) {
            throw new ForbiddenParticipantsCountException("The event has reached the limit of requests " +
                    "for participation");
        }
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(State.PENDING)
                .build();
        if (event.getParticipantLimit() == 0
                || event.getRequestModeration().equals(false)) {
            participationRequest.setStatus(State.CONFIRMED);
        }
        return ParticipationRequestMapper.toParticipationRequestDto(
                participationRequestRepository.save(participationRequest));
    }

    @Override
    public ParticipationRequestDto update(long userId, long requestId) {
        User requester = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User with id=%d was not found", userId)));
        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ParticipationRequestNotFoundException(
                        String.format("ParticipationRequest with id=%d was not found", requestId)));
        if (!requester.getId().equals(participationRequest.getRequester().getId())) {
            throw new ParticipationRequestNotFoundException(
                    String.format("ParticipationRequest with id=%d was not found", requestId));
        }
        participationRequest.setStatus(State.CANCELED);
        return ParticipationRequestMapper.toParticipationRequestDto(
                participationRequestRepository.save(participationRequest));
    }
}
