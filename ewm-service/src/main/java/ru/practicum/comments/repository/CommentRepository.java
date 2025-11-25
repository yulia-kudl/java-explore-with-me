package ru.practicum.comments.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.comments.dto.CommentStatus;
import ru.practicum.comments.entity.CommentEntity;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findAllByAuthorId(Long userId);

    List<CommentEntity> findAllByEventIdAndStatus(Long eventId, CommentStatus commentStatus, Pageable pageable);

    List<CommentEntity> findAllByStatus(CommentStatus status, Pageable pageable);

    long countByEventId(Long eventId);

    @Query("SELECT c.event.id, COUNT(c) FROM CommentEntity c WHERE c.event.id IN :eventIds GROUP BY c.event.id")
    List<Object[]> countCommentsByEventIds(@Param("eventIds") List<Long> eventIds);

}
