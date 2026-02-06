package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "event_similarity",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_a", "event_b"})
)
public class EventSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a", nullable = false)
    private Long eventA;

    @Column(name = "event_b", nullable = false)
    private Long eventB;

    @Column(nullable = false)
    private double score;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
