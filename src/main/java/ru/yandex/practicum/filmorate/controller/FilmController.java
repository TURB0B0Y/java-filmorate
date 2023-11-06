package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private final AtomicInteger filmId = new AtomicInteger(0);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@RequestBody Film film) {
        log.info("Запрос на создание фильма {}", film);
        validateFilm(film);
        film.setId(filmId.incrementAndGet());
        films.put(film.getId(), film);
        return film;
    }

    public static void validateFilm(Film film) {
        try {
            if (film.getName() == null || film.getName().trim().isEmpty())
                throw new ValidationException("Название не может быть пустым");
            if (film.getDescription() != null && film.getDescription().trim().length() > 200)
                throw new ValidationException("Максимальная длина описания — 200 символов");
            if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 11, 28)))
                throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
            if (film.getDuration() != null && film.getDuration() < 1)
                throw new ValidationException("продолжительность фильма должна быть положительной");
        } catch (Exception e) {
            log.warn("validation error", e);
            throw e;
        }
    }

    @PutMapping
    public Film editFilm(@RequestBody Film film) {
        log.info("Запрос на обновление фильма {}", film);
        if (film.getId() == null || !films.containsKey(film.getId()))
            throw new ValidationException("film not exists");
        validateFilm(film);
        films.put(film.getId(), film);
        return film;
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Запрос на получение всех фильмов");
        return films.values();
    }
}
