package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    void addFilm(Film film);

    void editFilm(Film film);

    Collection<Film> getAll();

    Film getById(int filmId);

    Collection<Film> getPopularFilms(int count);

    void addAppraiser(Film film, User user);

    boolean isFilmHasAppraiser(Film film, User user);

    void removeAppraiser(Film film, User user);
}
