package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {
    public Comment toComment(CommentDto commentDto) {
        return Comment.builder()
                .content(commentDto.getContent())
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
                .build();
    }
}
