package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class FilmValidateTests {


    @Test
    void filmNameIsNullTest() {
        Film film = new Film();
        assertThrows(ValidationException.class, () -> FilmController.validateFilm(film));
    }

    @Test
    void filmNameIsEmptyTest() {
        Film film = new Film();
        film.setName("");
        assertThrows(ValidationException.class, () -> FilmController.validateFilm(film));
    }

    @Test
    void filmNameIsSpaceTest() {
        Film film = new Film();
        film.setName("                  ");
        assertThrows(ValidationException.class, () -> FilmController.validateFilm(film));
    }

    @Test
    void filmDescLengthLimitTest() {
        Film film = new Film();
        film.setName("film");
        film.setName("*".repeat(201));
        assertThrows(ValidationException.class, () -> FilmController.validateFilm(film));
    }

    @Test
    void filmReleaseDateNullTest() {
        Film film = new Film();
        film.setName("film");
        film.setName("*".repeat(20));
        assertThrows(ValidationException.class, () -> FilmController.validateFilm(film));
    }

    @Test
    void filmReleaseDateTest() {
        Film film = new Film();
        film.setName("film");
        film.setName("*".repeat(20));
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> FilmController.validateFilm(film));
    }

    @Test
    void filmDurationTest() {
        Film film = new Film();
        film.setName("film");
        film.setName("*".repeat(20));
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(-1);
        assertThrows(ValidationException.class, () -> FilmController.validateFilm(film));
    }

    @Test
    void filmDurationZeroTest() {
        Film film = new Film();
        film.setName("film");
        film.setName("*".repeat(20));
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(0);
        assertThrows(ValidationException.class, () -> FilmController.validateFilm(film));
    }
}
