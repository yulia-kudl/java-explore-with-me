package ru.practicum.ErrorHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EventChangeException extends RuntimeException{
    public EventChangeException() {
        super("Only pending or canceled events can be changed");

    }
}
