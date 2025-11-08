package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.HitEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<HitEntity, Long> {

    @Query("SELECT h.app, h.uri, COUNT(h.id) " +
            "FROM HitEntity h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri")
    List<Object[]> getAllHits(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end,
                              @Param("uris") List<String> uris);

    // Только уникальные IP
    @Query("SELECT h.app, h.uri, COUNT(DISTINCT h.ip) " +
            "FROM HitEntity h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL  OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri")
    List<Object[]> getUniqueHits(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("uris") List<String> uris);


}
