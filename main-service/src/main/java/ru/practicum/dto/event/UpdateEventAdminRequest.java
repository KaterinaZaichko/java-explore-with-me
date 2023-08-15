package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest extends UpdateEventRequest {
    private StateAction stateAction;

    public enum StateAction {
        PUBLISH_EVENT,
        REJECT_EVENT
    }
}
