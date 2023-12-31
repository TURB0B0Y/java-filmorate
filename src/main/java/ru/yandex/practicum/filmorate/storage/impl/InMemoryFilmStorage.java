package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.SortingFilms;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final ConcurrentHashMap<Integer, Film> films = new ConcurrentHashMap<>();
    private final AtomicInteger filmId = new AtomicInteger(0);

    @Override
    public void addFilm(Film film) {
        film.setId(filmId.incrementAndGet());
        films.put(film.getId(), film);
    }

    @Override
    public void editFilm(Film film) {
        if (film.getId() == null || !films.containsKey(film.getId()))
            throw new NotFoundException("фильм с id %s не найден", film.getId());
        films.put(film.getId(), film);
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public Film getById(int filmId) {
        return films.get(filmId);
    }

    @Override
    public List<Film> getPopularFilms(int count, int genreId, int year) {
        return films.values().stream()
                .sorted((o1, o2) -> Integer.compare(o2.getAppraisers().size(), o1.getAppraisers().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public void addAppraiser(int filmId, int userId) {
        films.get(filmId).getAppraisers().add(userId);
    }

    @Override
    public boolean isFilmHasAppraiser(int filmId, int userId) {
        return films.get(filmId).getAppraisers().contains(userId);
    }

    @Override
    public void removeAppraiser(int filmId, int userId) {
        films.get(filmId).getAppraisers().remove(userId);
    }

    @Override
    public List<Film> getSortDirectorsOfFilms(int directorId, SortingFilms sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Film> searchMovieByTitleAndDirector(String query, List<String> by) {
        throw new UnsupportedOperationException("Функция пока не реализована");
    }

    @Override
    public List<Film> moviesSharedWithFriend(int userId, int friendId) {
        throw new UnsupportedOperationException("Функция пока не реализована");
    }

    @Override
    public void deleteFilmById(int id) {
        throw new UnsupportedOperationException("Функция пока не реализована");
    }

    @Override
    public List<Film> getRecommendations(int userId) {
        throw new UnsupportedOperationException("Функция пока не реализована");
    }
}
