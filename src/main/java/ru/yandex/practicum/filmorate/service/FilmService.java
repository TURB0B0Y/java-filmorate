package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public void addFilm(Film film) {
        filmStorage.addFilm(film);
    }

    public void editFilm(Film film) {
        filmStorage.editFilm(film);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getFilmById(int filmId) {
        Film film = filmStorage.getById(filmId);
        if (film == null)
            throw new NotFoundException("Фильм с id %s не найден", filmId);
        return film;
    }

    public void likeFilm(int id, int userId) {
        Film film = getFilmById(id);
        User user = userService.getUserById(userId); // check user exists
        if (!film.getAppraisers().add(user.getId()))
            throw new ConflictException("Пользователь %s уже оценил фильм %s", userId, id);
    }

    public void unLikeFilm(int id, int userId) {
        Film film = getFilmById(id);
        User user = userService.getUserById(userId); // check user exists
        if (!film.getAppraisers().remove(user.getId()))
            throw new ConflictException("Пользователь %s еще не оценивал фильм %s", userId, id);
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count); // логика в storage тк затем запрос будет идти в базу данных
    }
}
