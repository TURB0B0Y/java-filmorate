package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFriend;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.impl.UserFriendDbStorage;
import ru.yandex.practicum.filmorate.util.RandomUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@ActiveProfiles("test")
public class UserFriendDbStorageTest {
    private final UserFriendDbStorage userFriendDbStorage;
    private final UserDbStorage userStorage;

    @Autowired
    public UserFriendDbStorageTest(NamedParameterJdbcTemplate jdbcTemplate) {
        this.userFriendDbStorage = new UserFriendDbStorage(jdbcTemplate);
        this.userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    public void testFindByUserAndFriend() {
        User user = RandomUtils.getRandomUser();
        userStorage.addUser(user);
        User friend = RandomUtils.getRandomUser();
        userStorage.addUser(friend);
        UserFriend userFriend = new UserFriend(user.getId(), friend.getId());
        userFriendDbStorage.save(userFriend);
        UserFriend userFriendFromDB = userFriendDbStorage.findByUserAndFriend(user, friend).get();
        assertThat(userFriendFromDB).isNotNull().usingRecursiveComparison().isEqualTo(userFriend);
    }

    @Test
    public void testDelete() {
        User user = RandomUtils.getRandomUser();
        userStorage.addUser(user);
        User friend = RandomUtils.getRandomUser();
        userStorage.addUser(friend);
        UserFriend userFriend = new UserFriend(user.getId(), friend.getId());
        userFriendDbStorage.save(userFriend);
        userFriendDbStorage.delete(userFriend);
        Optional<UserFriend> userFriendFromDB = userFriendDbStorage.findByUserAndFriend(user, friend);
        assertThat(userFriendFromDB).isNotNull();
        assertThat(userFriendFromDB.isEmpty()).isEqualTo(true);
    }

    @Test
    public void testFindAllByUser() {
        User user = RandomUtils.getRandomUser();
        userStorage.addUser(user);
        User friend = RandomUtils.getRandomUser();
        userStorage.addUser(friend);
        UserFriend userFriend = new UserFriend(user.getId(), friend.getId());
        userFriendDbStorage.save(userFriend);
        Collection<UserFriend> allByUser = userFriendDbStorage.findAllByUser(user);
        Collection<UserFriend> allByFriend = userFriendDbStorage.findAllByUser(friend);
        assertThat(allByUser).isNotNull().usingRecursiveComparison().isEqualTo(Collections.singletonList(userFriend));
        assertThat(allByFriend).isNotNull();
        assertThat(allByFriend.isEmpty()).isEqualTo(true);
    }
}
