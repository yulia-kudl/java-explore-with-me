package ru.practicum.events.specifications;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.events.dto.EventState;
import ru.practicum.events.entity.EventEntity;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecifications {

    public static Specification<EventEntity> adminFilter(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (users != null && !users.isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(users));
            }
            if (states != null && !states.isEmpty()) {
                predicates.add(root.get("state").in(states));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            // eventDate >= rangeStart
            if (rangeStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }

            // eventDate <= rangeEnd
            if (rangeEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<EventEntity> publicFilter(
            String text,
            List<Long> categoryIdsLong,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //проверяю текст
            if (text != null && !text.isBlank()) {
                String pattern = "%" + text.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("annotation")), pattern),
                        cb.like(cb.lower(root.get("title")), pattern)));
            }

            predicates.add(root.get("state").in(EventState.PUBLISHED)); // только опубликованные

            if (categoryIdsLong != null && !categoryIdsLong.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIdsLong));
            }

            // eventDate >= rangeStart
            if (rangeStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }

            // eventDate <= rangeEnd
            if (rangeEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            if (paid != null) {
                predicates.add(cb.equal(root.get("paid"), paid));
            }
            if (onlyAvailable != null & Boolean.TRUE.equals(onlyAvailable)) {
                predicates.add(cb.equal(root.get("available"), onlyAvailable));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

