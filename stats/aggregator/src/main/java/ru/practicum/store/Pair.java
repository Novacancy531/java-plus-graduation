package ru.practicum.store;

import lombok.Getter;

@Getter
public final class Pair {

    private final long first;
    private final long second;

    public Pair(long a, long b) {
        this.first = Math.min(a, b);
        this.second = Math.max(a, b);
    }
}
