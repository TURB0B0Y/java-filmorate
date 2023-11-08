package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UserValidateTests {

    @Test
    void userEmailNullTest() {
        User user = new User();
        assertThrows(ValidationException.class, () -> UserController.validateUser(user));
    }

    @Test
    void userEmailEmptyTest() {
        User user = new User();
        user.setEmail("");
        assertThrows(ValidationException.class, () -> UserController.validateUser(user));
    }

    @Test
    void userEmailSpaceTest() {
        User user = new User();
        user.setEmail("     ");
        assertThrows(ValidationException.class, () -> UserController.validateUser(user));
    }

    @Test
    void userEmailFormatErrorTest() {
        User user = new User();
        user.setEmail("adafasfasf");
        assertThrows(ValidationException.class, () -> UserController.validateUser(user));
    }

    @Test
    void userLoginNullTest() {
        User user = new User();
        user.setEmail("email@mail.ru");
        assertThrows(ValidationException.class, () -> UserController.validateUser(user));
    }

    @Test
    void userLoginEmptyTest() {
        User user = new User();
        user.setEmail("email@mail.ru");
        user.setLogin("");
        assertThrows(ValidationException.class, () -> UserController.validateUser(user));
    }

    @Test
    void userLoginSpaceTest() {
        User user = new User();
        user.setEmail("email@mail.ru");
        user.setLogin(" ");
        assertThrows(ValidationException.class, () -> UserController.validateUser(user));
    }

    @Test
    void userLoginHasSpaceTest() {
        User user = new User();
        user.setEmail("email@mail.ru");
        user.setLogin("sd ");
        assertThrows(ValidationException.class, () -> UserController.validateUser(user));
    }

    @Test
    void userBridgeDayTest() {
        User user = new User();
        user.setEmail("email@mail.ru");
        user.setLogin("sd ");
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> UserController.validateUser(user));
    }

    @Test
    void userNameNullTest() {
        User user = new User();
        user.setEmail("email@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().minusDays(1));
        UserController.validateUser(user);
        assertEquals(user.getName(), user.getLogin());
    }

    @Test
    void userNameEmptyTest() {
        User user = new User();
        user.setEmail("email@mail.ru");
        user.setLogin("login");
        user.setName("");
        user.setBirthday(LocalDate.now().minusDays(1));
        UserController.validateUser(user);
        assertEquals(user.getName(), user.getLogin());
    }

    @Test
    void userNameSpaceTest() {
        User user = new User();
        user.setEmail("email@mail.ru");
        user.setLogin("login");
        user.setName("");
        user.setBirthday(LocalDate.now().minusDays(1));
        UserController.validateUser(user);
        assertEquals(user.getName(), user.getLogin());
    }

}
