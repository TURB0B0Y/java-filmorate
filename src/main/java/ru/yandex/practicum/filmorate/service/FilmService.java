package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.enums.SortingFilms;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    private final FeedStorage feedStorage;

    private final GenreStorage genreStorage;
    private final MotionPictureAssociationStorage motionPictureAssociationStorage;

    @Qualifier("directorDbStorage")
    private final DirectorStorage directorStorage;

    public void addFilm(Film film) {
        setMPA(film);
        setGenres(film);
        filmStorage.addFilm(film);
    }

    private void setMPA(Film film) {
        if (film.getMpa() != null) {
            MotionPictureAssociation mpa = motionPictureAssociationStorage.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с id %s не существует", film.getMpa().getId()));
            film.setMpa(mpa);
        }
    }

    public Film editFilm(Film film) {
        Film filmFromDB = getFilmById(film.getId());
        setMPA(film);
        filmFromDB.setMpa(film.getMpa());
        filmFromDB.setDuration(film.getDuration());
        filmFromDB.setName(film.getName());
        filmFromDB.setDescription(film.getDescription());
        filmFromDB.setReleaseDate(film.getReleaseDate());
        setGenres(film);
        filmFromDB.setGenres(film.getGenres());
        filmFromDB.setDirectors(film.getDirectors());
        filmStorage.editFilm(filmFromDB);
        return filmFromDB;
    }

    private void setGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty())
            return;
        Set<Integer> genres = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        film.setGenres(genreStorage.findAllById(genres));
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getFilmById(int filmId) {
        Film film = filmStorage.getById(filmId);
        if (film == null)
            throw new NotFoundException("Фильм с id %s не найден", filmId);
        film.setDirectors(directorStorage.getDirectorsForFilmId(filmId));
        return film;
    }

    public void likeFilm(int id, int userId) {
        filmStorage.addAppraiser(id, userId);
        feedStorage.createFeed(userId, id, EventType.LIKE, Operation.ADD, Instant.now().toEpochMilli());
    }

    public void unLikeFilm(int id, int userId) {
        if (!filmStorage.isFilmHasAppraiser(id, userId))
            throw new NotFoundException("Пользователь %s еще не оценивал фильм %s", userId, id);
        filmStorage.removeAppraiser(id, userId);
        feedStorage.createFeed(userId, id, EventType.LIKE, Operation.REMOVE, Instant.now().toEpochMilli());

    }

    public List<Film> getSortDirectorsOfFilms(int directorId, SortingFilms sort) {
        directorStorage.get(directorId);
        return filmStorage.getSortDirectorsOfFilms(directorId, sort);
    }

    public Collection<Film> searchMovieByTitleAndDirector(String query, List<String> by) {
        return filmStorage.searchMovieByTitleAndDirector(query, by);
    }

    public List<Film> moviesSharedWithFriend(int userId, int friendId) {
        return filmStorage.moviesSharedWithFriend(userId, friendId);
    }

    public void deleteFilmById(int id) {
        if (filmStorage.getById(id) == null)
            throw new NotFoundException("Фильм %s не существует, удаление невозможно", id);
        filmStorage.deleteFilmById(id);
    }

    public List<Film> getPopularFilms(int count, int genreId, int year) {
        return filmStorage.getPopularFilms(count, genreId, year);
    }

}
