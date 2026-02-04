package ru.practicum.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "user_event_interaction",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"})
)
public class UserEventInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private double weight;

    @Column(name = "last_ts", nullable = false)
    private OffsetDateTime lastTs;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public OffsetDateTime getLastTs() {
        return lastTs;
    }

    public void setLastTs(OffsetDateTime lastTs) {
        this.lastTs = lastTs;
    }
}
