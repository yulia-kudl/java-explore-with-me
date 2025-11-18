package ru.practicum.events.service;

public enum SortType {
    EVENT_DATE,
    VIEWS;

    public static SortType from(String str) {
        if (str == null) return EVENT_DATE;
        return switch (str.toUpperCase()) {
            case "EVENT_DATE" -> EVENT_DATE;
            case "VIEWS" -> VIEWS;
            default -> throw new IllegalArgumentException("Unknown sort type: " + str);
        };
    }
}