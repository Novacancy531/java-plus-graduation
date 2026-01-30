package ru.practicum.filter;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.annotation.DateRange;
import ru.practicum.annotation.DateRangeAware;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor

@DateRange
public class AdminEventsFilter implements DateRangeAware {

    List<Long> users;

    List<String> states;

    List<Long> categories;

    @NotNull(message = "фильтр: `Время запроса начало` обязательно к заполнению")
    private LocalDateTime rangeStart;

    @NotNull(message = "фильтр: `Время запроса окончание` обязательно к заполнению")
    private LocalDateTime rangeEnd;

    @Override
    public LocalDateTime getStart() {
        return rangeStart;
    }

    @Override
    public LocalDateTime getEnd() {
        return rangeEnd;
    }
}
