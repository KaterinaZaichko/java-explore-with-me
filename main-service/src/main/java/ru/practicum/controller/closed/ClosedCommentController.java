package ru.practicum.controller.closed;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.CommentService;

import javax.validation.Valid;

@RestController
@RequestMapping("/users/{userId}/comments")
@AllArgsConstructor
@Slf4j
@Validated
public class ClosedCommentController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto saveComment(@PathVariable long userId,
                                  @RequestParam long eventId,
                                  @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Creating comment");
        return commentService.save(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateRequest(@PathVariable long userId,
                                    @PathVariable long commentId,
                                    @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Updating comment");
        return commentService.update(userId, commentId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long userId,
                              @PathVariable long commentId) {
        log.info("Deleting comment");
        commentService.deleteByUser(userId, commentId);
    }
}
