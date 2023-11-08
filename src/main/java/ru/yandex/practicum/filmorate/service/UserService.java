package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void editUser(User user) {
        userStorage.editUser(user);
    }

    public void addUser(User user) {
        userStorage.addUser(user);
    }

    public User getUserById(int userId) {
        User user = userStorage.getById(userId);
        if (user == null)
            throw new NotFoundException("Пользователь с id %s не найден", userId);
        return user;
    }

    public void addFriend(int id, int friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        if (!user.getFriends().add(friend.getId()) || !friend.getFriends().add(user.getId()))
            throw new ConflictException("Пользователь %s уже дружит с %s", id, friendId);
    }

    public void deleteFriend(int id, int friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        if (!user.getFriends().remove(friend.getId()) || !friend.getFriends().remove(user.getId()))
            throw new ConflictException("Пользователь %s еще не дружит с %s", id, friendId);
    }

    public Collection<User> getFriends(int id) {
        User user = getUserById(id);
        return userStorage.getUsersById(user.getFriends());
    }

    public Collection<User> getCommonFriends(int id, int otherId) {
        User user = getUserById(id);
        User otherUser = getUserById(otherId);

        Set<Integer> commonFriends = new HashSet<>(user.getFriends());
        commonFriends.retainAll(new HashSet<>(otherUser.getFriends()));

        if (commonFriends.isEmpty())
            return Collections.emptyList();

        return userStorage.getUsersById(commonFriends);
    }
}
