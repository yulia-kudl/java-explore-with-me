package ru.practicum.comments;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentShortDto;
import ru.practicum.comments.dto.CommentStatus;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.service.CommentService;

import java.util.List;

@RestController
@RequestMapping(path = "/")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CommentController {
    private final CommentService commentService;

    // private
    // PUT /users/{userId}/events/{eventId}/comments - добавить коммент 201
    @PutMapping("users/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    CommentDto addComment(@PathVariable Long userId,
                          @PathVariable Long eventId,
                          @RequestBody @Valid NewCommentDto request) {

        return commentService.addComment(userId, eventId, request);

    }

    //PATCH /users/{userId}/comments/{commentId} - обновить коммент
    @PatchMapping("users/{userId}/comments/{commentId}")
    CommentDto updateComment(@PathVariable Long userId,
                             @PathVariable Long commentId,
                             @RequestBody @Valid NewCommentDto update) {
        return commentService.updateComment(userId, commentId, update);
    }
    // GET /users/{userId}/comments/{commentId} - получить свой коммент

    @GetMapping("/users/{userId}/comments/{commentId}")
    CommentDto getCommentByAuthor ( @PathVariable Long userId, @PathVariable Long commentId) {
        return commentService.getCommentByAuthor(userId, commentId);
    }
    // GET /users/{userId}/comments - получить свои комменты

    @GetMapping("/users/{userId}/comments")
    List<CommentDto> getAllCommentsByAuthor(@PathVariable Long userId){
        return  commentService.getAllCommentsByAuthor(userId);
    }
    // DELETE  /users/{userId}/comments/{commentId}
    @DeleteMapping("/users/{userId}/comments/{commentId}")
    void deleteCommentByAuthor(@PathVariable Long userId, @PathVariable Long commentId) {
        commentService.deleteCommentByAuthor(userId, commentId);
    }

    //public
    // GET /events/{eventId}/comments?size &from   (сортировка по дате деск)
    @GetMapping("/events/{eventId}/comments")
    List<CommentShortDto> getCommentsByEvent(@PathVariable Long eventId,
                                      @RequestParam(defaultValue ="0") Integer from,
                                      @RequestParam (defaultValue = "10") Integer size) {
        return commentService.getCommentsByEvent( eventId, from, size);
    }


    //Admin

    //GET /admin/comments
    @GetMapping("/admin/comments")
    List<CommentDto> getAllCommentsForAdmin(@RequestParam(required = false) CommentStatus status,
                                            @RequestParam(defaultValue ="0") Integer from,
                                            @RequestParam (defaultValue = "10") Integer size)  {
        return commentService.getAllCommentsForAdmin(status, from, size);
    }

    //  GET /admin/comments/{commentId}
    @GetMapping("/admin/comments/{commentId}")
    CommentDto getCommentForAdmin(@PathVariable Long commentId) {
        return commentService.getComment(commentId);
    }

    // PATCH /admin/comments/{commentId}/publish
    @PatchMapping("admin/comments/{commentId}/publish")
    CommentDto publishComment(@PathVariable Long commentId) {
        return commentService.publishComment(commentId);
    }

    // PATCH /admin/comments/{commentId}/reject
    @PatchMapping("admin/comments/{commentId}/reject")
    CommentDto rejectComment(@PathVariable Long commentId) {
        return commentService.rejectComment(commentId);
    }

    // DELETE /admin/comments/{commentId}
    @DeleteMapping("/admin/comments/{commentId}")
    void deleteCommentByAdmin(@PathVariable Long commentId) {
         commentService.deleteCommentByAdmin(commentId);
    }

}