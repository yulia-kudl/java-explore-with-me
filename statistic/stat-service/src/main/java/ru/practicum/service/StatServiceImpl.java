package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.HitRequestDTO;
import ru.practicum.StatsResponse;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;
    private final HitEntityMapper mapper;

    @Override
    public void addHit(HitRequestDTO requestDto) {
        statRepository.save(mapper.toEntity(requestDto));
    }

    @Override
    public List<StatsResponse> getStats(List<String> uris, LocalDateTime start, LocalDateTime end, boolean unique) {
        List<Object[]> stats;
        List<String> queryUris = (uris == null || uris.isEmpty()) ? null : uris;

        if (unique) {
            stats = statRepository.getUniqueHits(start, end, queryUris);
        } else {
            stats = statRepository.getAllHits(start, end, queryUris);
        }
        return stats.stream()
                .map(row -> {
                   Integer hits = ((Number) row[2]).intValue();
                    return new StatsResponse(
                            (String) row[0],
                            (String) row[1],
                            hits
                    );
                })
                .sorted(Comparator.comparingInt(StatsResponse::getHits).reversed())
                .collect(Collectors.toList());
    }
}
