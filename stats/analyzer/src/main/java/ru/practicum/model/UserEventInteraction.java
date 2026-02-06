package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
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
}
