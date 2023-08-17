package ru.practicum.service;

import ru.practicum.dto.comment.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto save(long userId, long eventId, CommentDto commentDto);

    CommentDto update(long userId, long commentId, CommentDto commentDto);

    void delete(long userId, long commentId);

    List<CommentDto> getAll(long eventId, int from, int size);
}
