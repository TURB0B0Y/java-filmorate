package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger userId = new AtomicInteger(0);

    @Override
    public void addUser(User user) {
        user.setId(userId.incrementAndGet());
        users.put(user.getId(), user);
    }

    @Override
    public void editUser(User user) {
        if (user.getId() == null || !users.containsKey(user.getId()))
            throw new NotFoundException("пользователь с id %s не найден", user.getId());
        users.put(user.getId(), user);
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public Optional<User> getById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getUsersById(Collection<Integer> ids) {
        return ids.stream().map(users::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Collection<Integer> getRecommendations(int userId) {
        return null;
    }
}
