package ru.practicum.dto.participationRequest;

import lombok.Builder;
import lombok.Data;
import ru.practicum.model.State;

@Data
@Builder
public class ParticipationRequestDto {
    private String created;
    private Long event;
    private Long id;
    private Long requester;
    private State status;
}
