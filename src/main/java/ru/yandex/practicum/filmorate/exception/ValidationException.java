package ru.yandex.practicum.filmorate.exception;

public class ValidationException extends APIException {

    public ValidationException(String message, Object... args) {
        super(message, args);
    }
}
