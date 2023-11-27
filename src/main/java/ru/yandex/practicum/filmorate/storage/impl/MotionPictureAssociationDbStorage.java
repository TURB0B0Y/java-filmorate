package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.storage.MotionPictureAssociationStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MotionPictureAssociationDbStorage implements MotionPictureAssociationStorage {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Collection<MotionPictureAssociation> findAll() {
        String sqlQuery = "select * from MOTION_PICTURE_ASSOCIATIONS";
        return jdbcTemplate.query(sqlQuery, this::mapToMpa);
    }

    private MotionPictureAssociation mapToMpa(ResultSet resultSet, int i) throws SQLException {
        MotionPictureAssociation mpa = new MotionPictureAssociation();
        mpa.setId(resultSet.getInt("mpa_id"));
        mpa.setName(resultSet.getString("name"));
        return mpa;
    }

    @Override
    public Optional<MotionPictureAssociation> findById(Integer id) {
        String sqlQuery = "select * from MOTION_PICTURE_ASSOCIATIONS where mpa_id = :mpaId";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sqlQuery,
                    new MapSqlParameterSource("mpaId", id),
                    this::mapToMpa
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
