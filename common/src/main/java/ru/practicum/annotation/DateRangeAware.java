package ru.practicum.annotation;

import java.time.LocalDateTime;

public interface DateRangeAware {

    LocalDateTime getStart();

    LocalDateTime getEnd();
}
