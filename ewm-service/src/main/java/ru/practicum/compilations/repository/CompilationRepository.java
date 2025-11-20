package ru.practicum.compilations.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.compilations.entity.CompilationEntity;

@Repository
public interface CompilationRepository extends JpaRepository<CompilationEntity, Long> {
    Page<CompilationEntity> findAllByPinned(Boolean pinned, Pageable pageable);
}
