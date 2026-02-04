package ru.practicum.store;

public final class Pair {

    private final long first;
    private final long second;

    public Pair(long a, long b) {
        this.first = Math.min(a, b);
        this.second = Math.max(a, b);
    }

    public long getFirst() {
        return first;
    }

    public long getSecond() {
        return second;
    }
}
