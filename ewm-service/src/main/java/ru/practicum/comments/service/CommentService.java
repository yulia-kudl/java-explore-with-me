package ru.practicum.comments.service;

import org.springframework.stereotype.Service;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentShortDto;
import ru.practicum.comments.dto.CommentStatus;
import ru.practicum.comments.dto.NewCommentDto;

import java.util.List;

@Service
public interface CommentService {
    CommentDto addComment(Long userId, Long eventId, NewCommentDto request);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto update);

    CommentDto getCommentByAuthor(Long userId, Long commentId);

    List<CommentDto> getAllCommentsByAuthor(Long userId);

    void deleteCommentByAuthor(Long userId, Long commentId);

    List<CommentShortDto> getCommentsByEvent(Long eventId, Integer from, Integer size);

    List<CommentDto> getAllCommentsForAdmin(CommentStatus status, Integer from, Integer size);

    CommentDto getComment(Long commentId);


    CommentDto updateCommentStatus(Long commentId, CommentStatus newStatus);

    void deleteCommentByAdmin(Long commentId);
}
