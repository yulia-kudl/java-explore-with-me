package ru.practicum.ErrorHandler;

public class RequestException extends RuntimeException{
    public RequestException(String message) {
        super(message);
    }
}
