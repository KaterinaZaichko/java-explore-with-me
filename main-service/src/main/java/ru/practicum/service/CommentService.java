package ru.practicum.service;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto save(long userId, long eventId, NewCommentDto newCommentDto);

    CommentDto update(long userId, long commentId, NewCommentDto newCommentDto);

    void deleteByUser(long userId, long commentId);

    void deleteByAdmin(long commentId);

    List<CommentDto> getAll(long eventId, int from, int size);
}
