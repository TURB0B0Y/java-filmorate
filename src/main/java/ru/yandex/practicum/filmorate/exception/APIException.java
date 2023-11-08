package ru.yandex.practicum.filmorate.exception;

public abstract class APIException extends RuntimeException {
    public APIException(String message, Object... args) {
        super(String.format(message, args));
    }
}
