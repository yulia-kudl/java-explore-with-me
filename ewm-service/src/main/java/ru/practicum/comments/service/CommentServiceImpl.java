package ru.practicum.comments.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ErrorHandler.ConflictException;
import ru.practicum.ErrorHandler.EntityNotFoundException;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentShortDto;
import ru.practicum.comments.dto.CommentStatus;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.entity.CommentEntity;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.events.dto.EventState;
import ru.practicum.events.entity.EventEntity;
import ru.practicum.events.repository.EventsRepository;
import ru.practicum.users.entity.UserEntity;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentEntityMapper mapper;
    private final UserRepository userRepository;
    private final EventsRepository eventsRepository;


    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto request) {
        // 404 - эвент не найден или статус не published
        // 404 - пользователь не найден
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(userId, "User"));

        EventEntity event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(eventId, "Event"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Cannot comment unpublished event id");
        }

        CommentEntity newEntity = mapper.toEntity(request);

        newEntity.setAuthor(user);
        newEntity.setEvent(event);
        newEntity.setStatus(CommentStatus.PENDING);
        newEntity.setCreatedOn(LocalDateTime.now());

        return mapper.toDto(commentRepository.save(newEntity));
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto update) {
        // коммент не принадлежит юзеру - 404
        // если rejected - конфликт 409
        // если published - переводим в пендинг
        // онбновляем updatedOn
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(commentId, "Comment"));
        if (!comment.getAuthor().getId().equals(commentId)) {
            throw new EntityNotFoundException(commentId, "Comment");
        }
        if (comment.getStatus().equals(CommentStatus.REJECTED)) {
            throw new ConflictException("Cannot update rejected comment");
        }
        comment.setText(update.getText());
        comment.setUpdatedOn(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);

        return mapper.toDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto getCommentByAuthor(Long userId, Long commentId) {
        // если не найден - возвращаем 404
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(commentId, "Comment"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new EntityNotFoundException(commentId, "Comment does not belong to user ");
        }

        return mapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getAllCommentsByAuthor(Long userId) {
        // если нет - пустой лист
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(userId, "User");
        }

        return commentRepository.findAllByAuthorId(userId)
                .stream()
                .map(mapper::toDto)
                .toList();

    }

    @Override
    @Transactional
    public void deleteCommentByAuthor(Long userId, Long commentId) {
        // если нет   коммента или юзера - 404
        // если связи юзерИд - комментИд - 409
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(userId, "User"));
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(commentId, "Comment"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ConflictException("Comment " + commentId + " does not belong to user " + userId);
        }

        commentRepository.delete(comment);
    }


    @Override
    public List<CommentShortDto> getCommentsByEvent(Long eventId, Integer from, Integer size) {
        // 404 - эвента нет
        //только паблишд сортировка по дате деск
        // пустой список если нет
        EventEntity event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(eventId, "event"));

        return commentRepository.findAllByEventIdAndStatus(eventId, CommentStatus.APPROVED, getPageable(from, size))
                .stream()
                .map(mapper::toShortDto)
                .toList();
    }

    @Override
    public List<CommentDto> getAllCommentsForAdmin(CommentStatus status, Integer from, Integer size) {

        List<CommentEntity> comments;

        if (status != null) {
            comments = commentRepository.findAllByStatus(status, getPageable(from, size));
        } else {
            comments = commentRepository.findAll(getPageable(from, size)).toList();
        }

        return comments.stream()
                .map(mapper::toDto)
                .toList();

    }

    @Override
    public CommentDto getComment(Long commentId) {
        // 404 не найден
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(commentId, "Comment"));

        return mapper.toDto(comment);
    }

    @Override
    public CommentDto updateCommentStatus(Long commentId, CommentStatus newStatus) {
        // если нет - 404
        // если статус не пендинг - 409
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(commentId, "Comment"));

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException(
                    "Comment " + commentId + " cannot be published - status not pending ");
        }

        comment.setStatus(newStatus);
        return mapper.toDto(commentRepository.save(comment));
    }


    @Override
    public void deleteCommentByAdmin(Long commentId) {
        // если нет - 404
        CommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(commentId, "Comment"));

        commentRepository.delete(entity);
    }


    private Pageable getPageable(Integer from, Integer size) {
        int page = from / size;
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn"));
    }
}
