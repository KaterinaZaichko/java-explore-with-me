package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.DateTimeConstant;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

@UtilityClass
public class ParticipationRequestMapper {
    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
        return ParticipationRequestDto.builder()
                .created(participationRequest.getCreated().format(DateTimeConstant.DATE_TIME_FORMATTER))
                .event(participationRequest.getEvent().getId())
                .id(participationRequest.getId())
                .requester(participationRequest.getRequester().getId())
                .status(participationRequest.getStatus())
                .build();
    }
}
