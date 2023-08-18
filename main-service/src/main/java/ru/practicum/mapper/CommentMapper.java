package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {
    public Comment toComment(NewCommentDto newCommentDto) {
        return Comment.builder()
                .content(newCommentDto.getContent())
                .created(LocalDateTime.now())
                .build();

    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .author(comment.getAuthor().getId())
                .event(comment.getEvent().getId())
                .content(comment.getContent())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .build();
    }
}
