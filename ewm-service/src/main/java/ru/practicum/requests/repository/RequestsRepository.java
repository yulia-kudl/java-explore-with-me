package ru.practicum.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.requests.dto.RequestStatus;
import ru.practicum.requests.entity.RequestEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestsRepository extends JpaRepository<RequestEntity, Long> {

    List<RequestEntity> findAllByUserId(Long userId);

    Optional<RequestEntity> findByUser_IdAndEvent_Id(Long userId, Long eventId);

    List<RequestEntity> findByUser_IdAndEvent_IdIn(Long userId, List<Long> eventIds);

    List<RequestEntity> findByEvent_IdAndStatus(Long eventId, RequestStatus requestStatus);

    List<RequestEntity> findByEvent_IdAndUser_IdAndStatus(Long eventId, Long userId, RequestStatus requestStatus);
}
