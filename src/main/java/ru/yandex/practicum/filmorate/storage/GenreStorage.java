package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    Collection<Genre> findAll();

    List<Genre> findAllById(Collection<Integer> ids);

    Optional<Genre> findById(Integer id);

    Genre save(Genre genre);
}
