package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFriend;

import java.util.Collection;
import java.util.Optional;

public interface UserFriendStorage {
    void save(UserFriend userFriend);

    Optional<UserFriend> findByUserAndFriend(int userId, int friendId);

    Collection<UserFriend> findAllByUser(User user);

    void delete(UserFriend userFriend);
}
