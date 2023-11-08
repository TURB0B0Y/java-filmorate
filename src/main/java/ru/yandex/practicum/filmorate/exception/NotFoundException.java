package ru.yandex.practicum.filmorate.exception;

public class NotFoundException extends APIException {
    public NotFoundException(String message, Object... args) {
        super(message, args);
    }
}
