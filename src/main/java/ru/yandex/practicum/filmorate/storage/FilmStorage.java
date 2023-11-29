package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.enums.SortingFilms;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    void addFilm(Film film);

    void editFilm(Film film);

    Collection<Film> getAll();

    Film getById(int filmId);

    Collection<Film> getPopularFilms(int count);

    void addAppraiser(int filmId, int userId);

    boolean isFilmHasAppraiser(int filmId, int userId);

    void removeAppraiser(int filmId, int userId);

    List<Film> getSortDirectorsOfFilms(int directorId, SortingFilms sort);

    List<Film> moviesSharedWithFriend(int userId, int friendId);

    void deleteFilmById(int id);

    public Collection<Film> searchMovieByTitleAndDirector(String query, List<String> by);
}
