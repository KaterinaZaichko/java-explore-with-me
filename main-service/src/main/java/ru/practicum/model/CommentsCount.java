package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommentsCount {
    private Event event;
    private Long countComments;
}
