package ru.practicum.dal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.exception.ConditionsException;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "authorId", nullable = false)
    private Long authorId;

    @Column(name = "eventId", nullable = false)
    private Long eventId;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "created",  nullable = false)
    private LocalDateTime created;

    @Column(name = "updated")
    private LocalDateTime updated;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    private Comment(Long authorId, Long eventId, String text, LocalDateTime created) {
        this.authorId = authorId;
        this.eventId = eventId;
        this.text = text;
        this.created = created;
        this.deleted = false;
    }

    public static Comment create(Long authorId, Long eventId, String text) {
        return new Comment(
                authorId,
                eventId,
                text,
                LocalDateTime.now()
        );
    }

    public void update (Long userId, String newText) {
        if (this.deleted) {
            throw new ConditionsException("Нельзя изменить удалённый комментарий");
        }

        if (!this.getAuthorId().equals(userId)) {
            throw new ConditionsException("Вы не можете редактировать данный комментарий.");
        }

        this.text = newText;
        this.updated = LocalDateTime.now();
    }

    public void delete(Long userId) {
        if (deleted) {
            return;
        }

        if (!authorId.equals(userId)) {
            throw new ConditionsException("Вы не можете удалить данный комментарий.");
        }

        this.deleted = true;
        this.updated = LocalDateTime.now();
    }

    public void deleteByAdmin() {
        if (deleted) {
            return;
        }

        this.deleted = true;
        this.updated = LocalDateTime.now();
    }
}
