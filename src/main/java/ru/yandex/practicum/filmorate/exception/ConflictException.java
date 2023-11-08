package ru.yandex.practicum.filmorate.exception;

public class ConflictException extends APIException {
    public ConflictException(String message, Object... args) {
        super(message, args);
    }
}
