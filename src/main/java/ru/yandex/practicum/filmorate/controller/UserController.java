package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private final ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger userId = new AtomicInteger(0);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody @Valid User user) {
        log.info("Запрос на создание пользователя {}", user);
        validateUser(user);
        user.setId(userId.incrementAndGet());
        users.put(user.getId(), user);
        return user;
    }

    public static void validateUser(User user) {
        try {
            if (user.getEmail() == null || user.getEmail().trim().isEmpty() || user.getEmail().indexOf('@') == -1)
                throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
            if (user.getLogin() == null || user.getLogin().trim().isEmpty() || user.getLogin().matches("\\s"))
                throw new ValidationException("логин не может быть пустым и содержать пробелы");
            if (user.getName() == null || user.getName().trim().isEmpty())
                user.setName(user.getLogin());
            if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now()))
                throw new ValidationException("дата рождения должна быть указана и не может быть в будущем");
        } catch (Exception e) {
            log.warn("validation error", e);
            throw e;
        }
    }

    @PutMapping
    public User editUser(@RequestBody @Valid User user) {
        log.info("Запрос на обновление пользователя {}", user);
        if (user.getId() == null || !users.containsKey(user.getId()))
            throw new ValidationException("user not exists");
        validateUser(user);
        users.put(user.getId(), user);
        return user;
    }

    @GetMapping
    public Collection<User> getAll() {
        log.info("Запрос на получение списка пользователей");
        return users.values();
    }
}
