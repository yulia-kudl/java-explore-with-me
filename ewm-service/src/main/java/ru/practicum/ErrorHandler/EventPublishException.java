package ru.practicum.ErrorHandler;

public class EventPublishException extends RuntimeException {
    public EventPublishException(String state) {
        super("Cannot publish the event because it's not in the right state: " + state);

    }

}
