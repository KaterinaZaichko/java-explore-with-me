package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ConfirmedRequestsCount {
    private Event event;
    private Long countConfirmedRequests;
}
