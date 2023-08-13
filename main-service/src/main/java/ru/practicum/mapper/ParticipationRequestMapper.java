package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class ParticipationRequestMapper {
    private final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
        return ParticipationRequestDto.builder()
                .created(participationRequest.getCreated().format(dtFormatter))
                .event(participationRequest.getEvent().getId())
                .id(participationRequest.getId())
                .requester(participationRequest.getRequester().getId())
                .status(participationRequest.getStatus())
                .build();
    }
}
