package ru.practicum.dal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.constant.RequestStatus;
import ru.practicum.exception.ConditionsException;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "eventId", nullable = false)
    private Long eventId;

    @Column(name = "requesterId", nullable = false)
    private Long requesterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    private Request(LocalDateTime created, Long eventId, Long requesterId, RequestStatus status) {
        this.created = created;
        this.eventId = eventId;
        this.requesterId = requesterId;
        this.status = status;
    }

    public static Request newRequest(long eventId, long requesterId, boolean isVerified) {
        return new Request(LocalDateTime.now(), eventId, requesterId, isVerified ? RequestStatus.CONFIRMED :
                RequestStatus.PENDING);
    }

    public void cancelBy(long userId) {
        if (this.requesterId != userId) {
            throw new ConditionsException("Только владелец может отменить заявку");
        }
        if (this.status == RequestStatus.REJECTED) {
            throw new ConditionsException("Нельзя отменить отклоненную заявку");
        }
        this.status = RequestStatus.CANCELED;
    }

    public void confirm() {
        ensurePending();
        this.status = RequestStatus.CONFIRMED;
    }

    public void reject() {
        ensurePending();
        this.status = RequestStatus.REJECTED;
    }

    private void ensurePending() {
        if (this.status != RequestStatus.PENDING) {
            throw new ConditionsException(
                    "Изменение статуса возможно только для заявки в статусе PENDING"
            );
        }
    }
}
