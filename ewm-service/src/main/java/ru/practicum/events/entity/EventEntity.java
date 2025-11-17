package ru.practicum.events.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @Column(columnDefinition = "TEXT", nullable = false)
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long confirmedRequests = 0L;
    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private UserEntity initiator;
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;
    @Column(name = "paid", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean paid = false;

    @Column(name = "participant_limit", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer participantLimit = 0;
    @Column(name = "published_on", nullable = true)
    private LocalDateTime publishedOn;
    @Column(name = "request_moderation", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean requestModeration = true;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventState state = EventState.PENDING;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;
   // @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    // private Boolean available = true;

    @Transient
    public Boolean getAvailable() {
        if (participantLimit == null || participantLimit == 0) return true; // нет лимита
        return participantLimit > (confirmedRequests != null ? confirmedRequests : 0);
    }

}

