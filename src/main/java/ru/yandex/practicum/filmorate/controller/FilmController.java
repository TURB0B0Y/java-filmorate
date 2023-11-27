package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private static final LocalDate AFTER_DATE = LocalDate.of(1895, 12, 28);
    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@RequestBody Film film) {
        log.info("Запрос на создание фильма {}", film);
        validateFilm(film);
        filmService.addFilm(film);
        return film;
    }

    public static void validateFilm(Film film) {
        try {
            if (film.getName() == null || film.getName().trim().isEmpty())
                throw new ValidationException("Название не может быть пустым");
            if (film.getDescription() != null && film.getDescription().trim().length() > 200)
                throw new ValidationException("Максимальная длина описания — 200 символов");
            if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(AFTER_DATE))
                throw new ValidationException("дата релиза — не раньше " + AFTER_DATE);
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
        validateFilm(film);
        return filmService.editFilm(film);
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Запрос на получение всех фильмов");
        return filmService.getAll();
    }

    @GetMapping("{filmId}")
    public Film getFilmById(@PathVariable int filmId) {
        log.info("Запрос на получение фильма {}", filmId);
        return filmService.getFilmById(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable int id, @PathVariable int userId) {
        log.info("Посталвен лайк фильму {} от пользователя {}", id, userId);
        filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void unLikeFilm(@PathVariable int id, @PathVariable int userId) {
        log.info("Убран лайк фильму {} от пользователя {}", id, userId);
        filmService.unLikeFilm(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос на получение списка популярных фильмов count={}", count);
        return filmService.getPopularFilms(count);
    }
}
