package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getAll() {
        return jdbcTemplate.query("select * from directors", DirectorDbStorage::createDirector);
    }

    @Override
    public Director get(int id) {
        try {
            return jdbcTemplate.queryForObject("select * from directors where director_id = ?", DirectorDbStorage::createDirector, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("В таблице нет одной записи с id = %s", id));
        }
    }

    @Override
    public Director create(Director data) {
        String sqlQuery = "INSERT INTO directors(name) " +
                "VALUES(?)";

        KeyHolder kayHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            stmt.setString(1, data.getName());
            return stmt;
        }, kayHolder);
        data.setId(kayHolder.getKey().intValue());
        log.debug("Добавлен новый элемент':" + data);
        return data;
    }

    @Override
    public Director update(Director data) {
        if (data.getId() == null) {
            throw new NotFoundException(String.format("Элемент %s не найден", data));
        }
        int id = get(data.getId()).getId();

        String sqlQuery = "UPDATE directors " +
                "SET " +
                "name = ? " +
                "WHERE director_id = ?";

        jdbcTemplate.update(sqlQuery,
                data.getName(),
                id);
        log.debug("Элемент обновлен:" + data);
        return data;
    }

    @Override
    public void delete(int id) {
        int directorId = get(id).getId();

        String sqlQuery = "DELETE FROM directors " +
                "WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery,
                directorId);
        log.debug("Директор удален с id: " + directorId);
    }

    public void deleteAllDirectorByFilm(int filmId) {
        String sqlQuery = "DELETE FROM film_directors WHERE film_id = ?";

        int count = jdbcTemplate.update(sqlQuery,
                filmId);
        log.debug(String.format("Удалены %s элементов'", count));
    }

    public void createDirectorByFilm(int directorId, int filmId) {
        String sqlQuery = "MERGE INTO film_directors(director_id, film_id) " +
                "VALUES(?, ?)";

        jdbcTemplate.update(sqlQuery,
                directorId,
                filmId);
        log.debug("Добавлен новый элемент'");
    }

    public List<Director> getDirectorIdsForFilmId(int filmId) {
        String sqlQuery = "SELECT d.director_id , d.NAME " +
                "FROM film_directors fd " +
                "LEFT JOIN FILMS f ON fd.FILM_ID = f.FILM_ID " +
                "LEFT JOIN directors d ON fd.director_id =d.director_id " +
                "WHERE f.FILM_ID = ?";

        return jdbcTemplate.query(sqlQuery, DirectorDbStorage::createDirector, filmId);
    }

    static Director createDirector(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getInt("director_id"))
                .name(rs.getString("name"))
                .build();
    }
}
