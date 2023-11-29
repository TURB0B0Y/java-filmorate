package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    void addUser(User user);

    void editUser(User user);

    Collection<User> getAll();

    Optional<User> getById(int id);

    Collection<User> getUsersById(Collection<Integer> ids);

    List<Integer> getRecommendations(int userId);

    void deleteUserById(int userId);
}
