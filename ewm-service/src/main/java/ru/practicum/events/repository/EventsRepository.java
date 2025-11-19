package ru.practicum.events.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.events.entity.EventEntity;

import java.time.LocalDateTime;

@Repository
public interface EventsRepository extends JpaRepository<EventEntity, Long>,
        JpaSpecificationExecutor<EventEntity> {

    Page<EventEntity> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    boolean existsByCategoryId(Long catId);

    @Query("SELECT MIN(e.publishedOn) FROM EventEntity e")
    LocalDateTime findMinPublishedDate();
}


