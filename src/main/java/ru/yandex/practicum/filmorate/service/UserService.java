package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFriend;
import ru.yandex.practicum.filmorate.storage.UserFriendStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final UserFriendStorage userFriendStorage;

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void editUser(User user) {
        User userFromDB = userStorage.getById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id %s не найден", user.getId()));
        userFromDB.setLogin(user.getLogin());
        userFromDB.setName(user.getName());
        userFromDB.setEmail(user.getEmail());
        userFromDB.setBirthday(user.getBirthday());
        userStorage.editUser(user);
    }

    public void addUser(User user) {
        userStorage.addUser(user);
    }

    public User getUserById(int userId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id %s не найден", userId));
        Collection<UserFriend> friends = userFriendStorage.findAllByUser(user);
        user.setFriends(friends);
        return user;
    }

    public void addFriend(int id, int friendId) {
        if (id == friendId)
            throw new ConflictException("Нельзя добавить себя в друзья");
        if (userFriendStorage.findByUserAndFriend(id, friendId).isPresent())
            throw new ConflictException("Пользователь %s уже дружит с %s", id, friendId);
        UserFriend userFriend = new UserFriend();
        userFriend.setUserId(id);
        userFriend.setFriendId(friendId);
        userFriend.setStatus(0);
        try {
            userFriendStorage.save(userFriend);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Один из указанных пользователей не существует [%s, %s]", id, friendId);
        }
    }

    public void deleteFriend(int id, int friendId) {
        if (id == friendId)
            throw new ConflictException("Нельзя удалить себя из друзья");
        Optional<UserFriend> userFriendOptional = userFriendStorage.findByUserAndFriend(id, friendId);
        if (userFriendOptional.isEmpty())
            throw new ConflictException("Запрос в друзья не найден");
        userFriendStorage.delete(userFriendOptional.get());
    }

    public Collection<User> getFriends(int id) {
        User user = getUserById(id);
        return userStorage.getUsersById(
                userFriendStorage.findAllByUser(user).stream()
                        .map(userFriend -> mapToUser(userFriend, user)).collect(Collectors.toList())
        );
    }

    private static Integer mapToUser(UserFriend userFriend, User user) {
        if (userFriend.getUserId().equals(user.getId()))
            return userFriend.getFriendId();
        return userFriend.getUserId();
    }

    public Collection<User> getCommonFriends(int id, int otherId) {
        User user = getUserById(id);
        Collection<UserFriend> userFriends = userFriendStorage.findAllByUser(user);
        if (id == otherId)
            return userStorage.getUsersById(
                    userFriends.stream().map(userFriend -> mapToUser(userFriend, user)).collect(Collectors.toList())
            );

        User otherUser = getUserById(otherId);
        Collection<UserFriend> otherUserFriends = userFriendStorage.findAllByUser(otherUser);
        Set<Integer> commonFriends = userFriends.stream().map(UserFriend::getFriendId).collect(Collectors.toSet());
        commonFriends.retainAll(otherUserFriends.stream().map(UserFriend::getFriendId).collect(Collectors.toSet()));

        if (commonFriends.isEmpty())
            return Collections.emptyList();

        return userStorage.getUsersById(commonFriends);
    }
}
