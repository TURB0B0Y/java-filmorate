package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {

    List<Director> getAll();

    Director get(int id);

    Director create(Director data);

    Director update(Director data);

    void delete(int data);

    void deleteAllDirectorByFilm(int filmId);

    void createDirectorByFilm(int directorId, int filmId);

    List<Director> getDirectorsForFilmId(int filmId);
}
