package ru.yandex.practicum.filmorate.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResponseDTO<T> {
    private final T response;
}
