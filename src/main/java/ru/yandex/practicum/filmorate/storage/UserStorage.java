package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    void addUser(User user);

    void editUser(User user);

    Collection<User> getAll();

    User getById(int id);

    Collection<User> getUsersById(Collection<Integer> ids);
}
