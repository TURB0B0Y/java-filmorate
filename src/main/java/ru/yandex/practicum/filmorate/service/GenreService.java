package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    @Transactional(readOnly = true)
    public Collection<Genre> getAll() {
        return genreStorage.findAll();
    }

    @Transactional(readOnly = true)
    public Genre getById(int genreId) {
        return genreStorage.findById(genreId)
                .orElseThrow(() -> new NotFoundException("Жанр с id %s не существует", genreId));
    }
}
