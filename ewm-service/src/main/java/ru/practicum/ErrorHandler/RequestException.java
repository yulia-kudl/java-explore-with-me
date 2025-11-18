package ru.practicum.ErrorHandler;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RequestException extends RuntimeException {
    public RequestException(String message) {
        super(message);
    }
}
