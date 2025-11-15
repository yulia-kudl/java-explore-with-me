package ru.practicum.ErrorHandler;

public class EventChangeException extends RuntimeException{
    public EventChangeException() {
        super("Only pending or canceled events can be changed");

    }
}
