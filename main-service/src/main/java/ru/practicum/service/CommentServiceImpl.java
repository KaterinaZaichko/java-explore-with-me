package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.exception.ForbiddenCommentException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public CommentDto save(long userId, long eventId, NewCommentDto newCommentDto) {
        User author = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                String.format("User with id=%d was not found", userId)));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Event with id=%d was not found", eventId)));
        if (event.getPublishedOn() == null) {
            throw new ForbiddenCommentException("This event not published yet");
        }
        Comment comment = CommentMapper.toComment(newCommentDto);
        comment.setAuthor(author);
        comment.setEvent(event);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto update(long userId, long commentId, NewCommentDto newCommentDto) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(String.format("User with id=%d was not found", userId));
        }
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Comment with id=%d was not found", commentId)));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenCommentException(String.format(
                    "The user with id=%d is not the author of the comment with id=%d", userId, commentId));
        }
        if (LocalDateTime.now().isAfter(comment.getCreated().plusHours(1))) {
            throw new ForbiddenCommentException("More than one hour has passed since the comment was created, " +
                    "editing is not possible");
        }
        comment.setContent(newCommentDto.getContent());
        comment.setUpdated(LocalDateTime.now());
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public void deleteByUser(long userId, long commentId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(String.format("User with id=%d was not found", userId));
        }
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Comment with id=%d was not found", commentId)));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenCommentException(String.format(
                    "The user with id=%d is not the author of the comment with id=%d", userId, commentId));
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteByAdmin(long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException(String.format("Comment with id=%d was not found", commentId));
        } else {
            commentRepository.deleteById(commentId);
        }
    }

    @Override
    public List<CommentDto> getAll(long eventId, int from, int size) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Event with id=%d was not found", eventId)));
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        return commentRepository.findAllByEvent(event, pageWithSomeElements).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
