package ru.practicum;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ArgumentException extends RuntimeException {
    public ArgumentException(String message) {
        super(message);
    }

}
