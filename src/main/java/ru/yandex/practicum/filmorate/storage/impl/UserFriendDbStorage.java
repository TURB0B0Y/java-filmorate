package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFriend;
import ru.yandex.practicum.filmorate.storage.UserFriendStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserFriendDbStorage implements UserFriendStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void save(UserFriend userFriend) {
        String sqlQuery = "insert into USER_FRIENDS(user_id, friend_id, status) values (:userId, :friendId, :status)";
        jdbcTemplate.update(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("userId", userFriend.getUserId())
                        .addValue("friendId", userFriend.getFriendId())
                        .addValue("status", userFriend.getStatus())
        );
    }

    @Override
    public Optional<UserFriend> findByUserAndFriend(int userId, int friendId) {
        String sqlQuery = "select * from USER_FRIENDS where (user_id = :userId and friend_id = :friendId) or (user_id = :friendId and friend_id = :userId)";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sqlQuery,
                    new MapSqlParameterSource()
                            .addValue("userId", userId)
                            .addValue("friendId", friendId),
                    this::mapToUserFriend
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private UserFriend mapToUserFriend(ResultSet resultSet, int rowNum) throws SQLException {
        UserFriend userFriend = new UserFriend();
        userFriend.setUserId(resultSet.getInt("user_id"));
        userFriend.setFriendId(resultSet.getInt("friend_id"));
        userFriend.setStatus(resultSet.getInt("status"));
        return userFriend;
    }

    @Override
    public Collection<UserFriend> findAllByUser(User user) {
        String sqlQuery = "select * from USER_FRIENDS where user_id = :userId";
        return jdbcTemplate.query(
                sqlQuery,
                new MapSqlParameterSource().addValue("userId", user.getId()),
                this::mapToUserFriend
        );
    }

    @Override
    public void delete(UserFriend userFriend) {
        String sqlQuery = "delete from USER_FRIENDS where user_id = :userId and friend_id = :friendId";
        jdbcTemplate.update(sqlQuery, new MapSqlParameterSource("userId", userFriend.getUserId())
                .addValue("friendId", userFriend.getFriendId()));
    }
}
