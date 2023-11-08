package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody @Valid User user) {
        log.info("Запрос на создание пользователя {}", user);
        validateUser(user);
        userService.addUser(user);
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
        userService.editUser(user);
        validateUser(user);
        return user;
    }

    @GetMapping("{userId}")
    public User getUserById(@PathVariable int userId) {
        log.info("Запрос на получение пользователя {}", userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public Collection<User> getAll() {
        log.info("Запрос на получение списка пользователей");
        return userService.getAll();
    }

    @PutMapping("{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Запрос на добавление в друзья {} {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Запрос на удаление из друзей {} {}", id, friendId);
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable int id) {
        log.info("Запрос на получение списка друзей {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        log.info("Запрос на получение списка общих друзей {} {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
