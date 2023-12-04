package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = "insert into USERS(login, email, name, birthday) values (:login, :email, :name, :birthday)";
        jdbcTemplate.update(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("login", user.getLogin())
                        .addValue("email", user.getEmail())
                        .addValue("name", user.getName())
                        .addValue("birthday", user.getBirthday()),
                keyHolder
        );
        user.setId(keyHolder.getKey().intValue());
    }

    @Override
    public void editUser(User user) {
        String sqlQuery = "update USERS set login = :login, email = :email, name = :name, birthday = :birthday where user_id = :userId";
        jdbcTemplate.update(sqlQuery,
                new MapSqlParameterSource()
                        .addValue("login", user.getLogin())
                        .addValue("email", user.getEmail())
                        .addValue("name", user.getName())
                        .addValue("birthday", user.getBirthday())
                        .addValue("userId", user.getId())
        );
    }

    @Override
    public Collection<User> getAll() {
        String sqlQuery = "select * from USERS";
        return jdbcTemplate.query(sqlQuery, this::mapToUser);
    }

    private User mapToUser(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("user_id"));
        user.setLogin(resultSet.getString("login"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setBirthday(resultSet.getObject("birthday", LocalDate.class));
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        String sqlQuery = "select * from USERS where user_id = :id";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sqlQuery,
                    new MapSqlParameterSource().addValue("id", id),
                    this::mapToUser
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<User> getUsersById(Collection<Integer> ids) {
        if (ids.isEmpty())
            return Collections.emptyList();
        String sqlQuery = "select * from USERS where user_id IN (:ids)";
        return jdbcTemplate.query(sqlQuery, new MapSqlParameterSource("ids", ids), this::mapToUser);
    }

    @Override
    public void deleteUserById(int id) {
        String sqlQuery = "delete from USERS where user_id = :userId";
        jdbcTemplate.update(sqlQuery, new MapSqlParameterSource().addValue("userId", id));
    }
}
