package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> findAll() {
        String sqlQuery = "select * from GENRES";
        return jdbcTemplate.query(sqlQuery, this::mapToGenre);
    }

    @Override
    public List<Genre> findAllById(Collection<Integer> ids) {
        String sqlQuery = "select * from GENRES where genre_id in (:ids)";
        return jdbcTemplate.query(
                sqlQuery,
                new MapSqlParameterSource("ids", ids),
                this::mapToGenre
        );
    }

    private Genre mapToGenre(ResultSet resultSet, int i) throws SQLException {
        Genre genre = new Genre();
        genre.setId(resultSet.getInt("genre_id"));
        genre.setName(resultSet.getString("name"));
        return genre;
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        String sqlQuery = "select * from GENRES where genre_id = :genreId";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sqlQuery,
                    new MapSqlParameterSource("genreId", id),
                    this::mapToGenre
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Genre save(Genre genre) {
        if (genre.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            String sqlQuery = "insert into GENRES (name) values (:name)";
            jdbcTemplate.update(
                    sqlQuery,
                    new MapSqlParameterSource().addValue("name", genre.getName()),
                    keyHolder
            );
            genre.setId(keyHolder.getKey().intValue());
        } else {
            String sqlQuery = "update GENRES set name = :name where genre_id = :genreId";
            jdbcTemplate.update(sqlQuery,
                    new MapSqlParameterSource()
                            .addValue("name", genre.getName())
                            .addValue("genreId", genre.getId())
            );
        }
        return genre;
    }
}
