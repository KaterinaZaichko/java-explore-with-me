package ru.practicum.dto.event;

import lombok.Data;
import ru.practicum.model.State;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private State status;
}
