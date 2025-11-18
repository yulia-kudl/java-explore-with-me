package ru.practicum.ErrorHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Long id, String entityName) {
        super(entityName + " with id=" + id + " was not found");
    }
}