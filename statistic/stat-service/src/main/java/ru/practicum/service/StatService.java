package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.HitRequestDTO;
import ru.practicum.StatsResponse;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface StatService {
    void addHit(HitRequestDTO requestDto);

    List<StatsResponse> getStats(List<String> uris, LocalDateTime start, LocalDateTime end, boolean unique);
}
