package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.SortingFilms;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MotionPictureAssociationStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

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
        return enrichingDirectorsToFilms(filmStorage.getAll());
    }

    public Film getFilmById(int filmId) {
        Film film = filmStorage.getById(filmId);
        if (film == null)
            throw new NotFoundException("Фильм с id %s не найден", filmId);
        film.setDirectors(directorStorage.getDirectorIdsForFilmId(filmId));
        return film;
    }

    public void likeFilm(int id, int userId) {
        if (filmStorage.isFilmHasAppraiser(id, userId))
            throw new ConflictException("Пользователь %s уже оценил фильм %s", userId, id);
        filmStorage.addAppraiser(id, userId);
    }

    public void unLikeFilm(int id, int userId) {
        if (!filmStorage.isFilmHasAppraiser(id, userId))
            throw new NotFoundException("Пользователь %s еще не оценивал фильм %s", userId, id);
        filmStorage.removeAppraiser(id, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return enrichingDirectorsToFilms(filmStorage.getPopularFilms(count));
    }

    public List<Film> getSortDirectorsOfFilms(int directorId, SortingFilms sort) {
        directorStorage.get(directorId);
        return enrichingDirectorsToFilms(filmStorage.getSortDirectorsOfFilms(directorId, sort));
    }

    private List<Film> enrichingDirectorsToFilms(Collection<Film> films) {
        List<Film> fullFilms = new ArrayList<>();
        if (films != null) {
            for (Film film : films) {
                film.setDirectors(directorStorage.getDirectorIdsForFilmId(film.getId()));
                fullFilms.add(film);
            }
        }
        return fullFilms;
    }

    public Collection<Film> searchMovieByTitleAndDirector(String query, List<String> by) {
        return enrichingDirectorsToFilms(filmStorage.searchMovieByTitleAndDirector(query, by));
    }


}
