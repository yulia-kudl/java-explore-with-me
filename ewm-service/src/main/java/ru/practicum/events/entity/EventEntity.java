package ru.practicum.events.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.categories.entity.CategoryEntity;
import ru.practicum.events.dto.EventState;
import ru.practicum.users.entity.UserEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "events")
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;
    private Long confirmed_requests;
    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    private LocalDateTime created_on;
    private String description;
    @Column(nullable = false)
    private LocalDateTime event_date;
    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private UserEntity initiator;
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;
    private Boolean paid;
    private Integer participant_limit;
    private LocalDateTime published_on;
    private Boolean request_moderation;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventState state = EventState.PENDING;
    @Column(nullable = false)
    private String title;
    private Boolean available;
}

