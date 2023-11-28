package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    @Qualifier("directorDbStorage")
    private final DirectorStorage storage;

    public List<Director> getAll() {
        return new ArrayList<>(storage.getAll());
    }

    public Director get(int id) {
        return storage.get(id);
    }

    public Director create(Director data) {
        return storage.create(data);
    }

    public Director update(Director data) {
        return storage.update(data);
    }

    public void delete(int id) {
        storage.delete(id);
    }

}
