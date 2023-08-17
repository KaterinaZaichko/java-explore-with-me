package ru.practicum.controller.closed;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@AllArgsConstructor
@Slf4j
@Validated
public class ClosedCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getComments(@RequestParam long eventId,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Getting comments");
        return commentService.getAll(eventId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto saveComment(@PathVariable long userId,
                                  @RequestParam long eventId,
                                  @RequestBody @Valid CommentDto commentDto) {
        log.info("Creating comment");
        return commentService.save(userId, eventId, commentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateRequest(@PathVariable long userId,
                                    @PathVariable long commentId,
                                    @RequestBody @Valid CommentDto commentDto) {
        log.info("Updating comment");
        return commentService.update(userId, commentId, commentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long userId,
                              @PathVariable long commentId) {
        log.info("Deleting comment");
        commentService.delete(userId, commentId);
    }
}
