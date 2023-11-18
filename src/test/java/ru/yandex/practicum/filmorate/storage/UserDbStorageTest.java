package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.util.RandomUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@ActiveProfiles("test")
public class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Autowired
    public UserDbStorageTest(NamedParameterJdbcTemplate jdbcTemplate) {
        this.userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    public void testFindUserById() {
        User newUser = RandomUtils.getRandomUser();
        userStorage.addUser(newUser);
        User savedUser = userStorage.getById(newUser.getId()).get();
        assertThat(savedUser).isNotNull().usingRecursiveComparison().isEqualTo(newUser);
    }

    @Test
    public void testFindUsersById() {
        List<User> users = Arrays.asList(
                RandomUtils.getRandomUser(),
                RandomUtils.getRandomUser(),
                RandomUtils.getRandomUser(),
                RandomUtils.getRandomUser()
        );
        for (User user : users)
            userStorage.addUser(user);
        Collection<User> usersFromDB = userStorage.getUsersById(users.stream().map(User::getId).collect(Collectors.toList()));
        assertThat(usersFromDB.isEmpty()).isEqualTo(false);
        assertThat(usersFromDB).isNotNull().usingRecursiveComparison().isEqualTo(users);
    }

    @Test
    public void testEditUser() {
        User user = RandomUtils.getRandomUser();
        userStorage.addUser(user);
        User edit = RandomUtils.getRandomUser();
        edit.setId(user.getId());
        userStorage.editUser(edit);
        User savedUser = userStorage.getById(user.getId()).get();
        assertThat(savedUser).isNotNull().usingRecursiveComparison().isEqualTo(edit);
    }

    @Test
    public void testFindAllUsers() {
        User newUser = RandomUtils.getRandomUser();
        userStorage.addUser(newUser);
        Collection<User> users = userStorage.getAll();
        assertThat(users.isEmpty()).isEqualTo(false);
    }

}
