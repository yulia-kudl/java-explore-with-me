package ru.practicum.ErrorHandler;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Long id, String entityName) {
        super(entityName +" with id=" + id + " was not found");
    }
}