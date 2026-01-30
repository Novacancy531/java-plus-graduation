package ru.practicum.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.annotation.DateRange;
import ru.practicum.annotation.DateRangeAware;
import ru.practicum.constant.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor

@DateRange
public class EventsFilter implements DateRangeAware {

    private String text;

    private Boolean paid;

    private List<Long> users;
    private List<String> states;

    private List<Long> categories;

    private LocalDateTime rangeStart;

    private LocalDateTime rangeEnd;

    private Boolean onlyAvailable = false;

    @Override
    public LocalDateTime getStart() {
        return rangeStart;
    }

    @Override
    public LocalDateTime getEnd() {
        return rangeEnd;
    }

    // Утилитный метод для получения states как EventState (чтобы не дублировать в сервисе)
    public List<EventState> getStatesAsEnum() {
        if (this.states == null || this.states.isEmpty()) {
            return null;
        }
        return this.states.stream()
                .map(EventState::valueOf)
                .toList();
    }
}