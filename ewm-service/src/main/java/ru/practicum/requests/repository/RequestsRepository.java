package ru.practicum.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.requests.entity.RequestEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestsRepository extends JpaRepository<RequestEntity, Long> {

    List<RequestEntity> findAllByUserId(Long userId);

    Optional<RequestEntity> findByUser_IdAndEvent_Id(Long userId, Long eventId);


    List<RequestEntity> findByEvent_Id(Long eventId);


    List<RequestEntity> findByIdIn(List<Long> requestIds);
}
