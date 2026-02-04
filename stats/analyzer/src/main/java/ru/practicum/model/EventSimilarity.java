package ru.practicum.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

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

    public Long getId() {
        return id;
    }

    public Long getEventA() {
        return eventA;
    }

    public void setEventA(Long eventA) {
        this.eventA = eventA;
    }

    public Long getEventB() {
        return eventB;
    }

    public void setEventB(Long eventB) {
        this.eventB = eventB;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
