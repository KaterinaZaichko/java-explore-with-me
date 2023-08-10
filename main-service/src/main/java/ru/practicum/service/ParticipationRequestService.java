package ru.practicum.service;

import ru.practicum.dto.participationRequest.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getAll(long userId);

    ParticipationRequestDto save(long userId, long eventId);

    ParticipationRequestDto update(long userId, long requestId);
}
