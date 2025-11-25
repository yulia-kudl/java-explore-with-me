package ru.practicum.comments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.comments.dto.CommentStatus;
import ru.practicum.events.entity.EventEntity;
import ru.practicum.users.entity.UserEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;
    @CreationTimestamp
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
    @Column(name = "updated_on", nullable = true)
    private LocalDateTime updatedOn;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status = CommentStatus.PENDING;
}
