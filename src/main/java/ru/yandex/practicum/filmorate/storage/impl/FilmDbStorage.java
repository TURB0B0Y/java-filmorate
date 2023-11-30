package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private static final String BASE_SELECT = "select" +
            " f.film_id as film_id," +
            " f.name as film_name," +
            " f.description as film_description," +
            " f.release_date as film_release_date," +
            " f.duration as film_duration," +
            " mpa.mpa_id as mpa_id," +
            " mpa.name as mpa_name";

    private static final String LIKES_EXIST_QUERY = "SELECT FILM_ID, COUNT(USER_ID) AS count " +
            "FROM APPRAISERS a GROUP BY FILM_ID";
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = "insert into FILMS(name, description, release_date, duration, mpa_id) " +
                "values (:name, :description, :releaseDate, :duration, :mpaId)";
        jdbcTemplate.update(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("name", film.getName())
                        .addValue("description", film.getDescription())
                        .addValue("releaseDate", film.getReleaseDate())
                        .addValue("duration", film.getDuration())
                        .addValue("mpaId", film.getMpa().getId()),
                keyHolder
        );
        film.setId(keyHolder.getKey().intValue());
        if (film.getGenres() == null)
            return;
        String genresUpdateQuery = "insert into FILM_GENRES (film_id, genre_id) VALUES (:filmId, :genreId)";
        jdbcTemplate.batchUpdate(
                genresUpdateQuery,
                film.getGenres().stream()
                        .map(genre -> new MapSqlParameterSource("genreId", genre.getId())
                                .addValue("filmId", film.getId()))
                        .toArray(SqlParameterSource[]::new)
        );
    }

    @Override
    public void editFilm(Film film) {
        String sqlQuery = "update FILMS set name = :name, description = :description, release_date = :releaseDate, " +
                "duration = :duration, mpa_id = :mpaId where film_id = :filmId";
        jdbcTemplate.update(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("name", film.getName())
                        .addValue("description", film.getDescription())
                        .addValue("releaseDate", film.getReleaseDate())
                        .addValue("duration", film.getDuration())
                        .addValue("mpaId", film.getMpa().getId())
                        .addValue("filmId", film.getId())
        );

        String getFilmGenresQuery = "select genre_id from FILM_GENRES where film_id = :filmId";
        List<Integer> oldGenres = jdbcTemplate.query(
                getFilmGenresQuery,
                new MapSqlParameterSource("filmId", film.getId()),
                (rs, rowNum) -> rs.getInt("genre_id")
        );
        Set<Integer> actualGenreIds = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        oldGenres.removeIf(actualGenreIds::remove);
        jdbcTemplate.update(
                "delete from FILM_GENRES where film_id = :filmId and genre_id in (:removeIds)",
                new MapSqlParameterSource("removeIds", oldGenres)
                        .addValue("filmId", film.getId())
        );
        String updateGenresQuery = "insert into FILM_GENRES (film_id, genre_id) values (:filmId, :genreId)";
        jdbcTemplate.batchUpdate(
                updateGenresQuery,
                actualGenreIds.stream().map(genreId -> new MapSqlParameterSource("genreId", genreId)
                        .addValue("filmId", film.getId())).toArray(SqlParameterSource[]::new)
        );
    }

    @Override
    public Collection<Film> getAll() {
        List<Film> films = jdbcTemplate.query(
                BASE_SELECT + " from FILMS f join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id",
                this::mapToFilm
        );
        fillFilms(films);
        return films;
    }

    private void fillFilms(List<Film> films) {
        Map<Integer, Film> filmsMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film, (t, t2) -> t));
        List<Integer> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        List<Map.Entry<Integer, Genre>> filmGenres = jdbcTemplate.query(
                "select fg.film_id as film_id, g.genre_id as genre_id, g.name as genre_name from FILM_GENRES fg " +
                        "join GENRES g on g.genre_id = fg.genre_id where fg.film_id in (:filmIds)",
                new MapSqlParameterSource("filmIds", filmIds),
                this::mapToFilmGenre
        );
        for (Map.Entry<Integer, Genre> entry : filmGenres) {
            Film film = filmsMap.get(entry.getKey());
            film.getGenres().add(entry.getValue());
        }
        List<Map.Entry<Integer, Integer>> filmAppraisers = jdbcTemplate.query(
                "select * from APPRAISERS where film_id in (:filmIds)",
                new MapSqlParameterSource("filmIds", filmIds),
                this::mapToFilmAppraiser
        );
        for (Map.Entry<Integer, Integer> entry : filmAppraisers) {
            Film film = filmsMap.get(entry.getKey());
            film.getAppraisers().add(entry.getValue());
        }
    }

    private Map.Entry<Integer, Integer> mapToFilmAppraiser(ResultSet rs, int i) throws SQLException {
        int filmId = rs.getInt("film_id");
        int appraiserId = rs.getInt("user_id");
        return new AbstractMap.SimpleEntry<>(filmId, appraiserId);
    }

    private Map.Entry<Integer, Genre> mapToFilmGenre(ResultSet rs, int i) throws SQLException {
        int filmId = rs.getInt("film_id");
        Genre genre = new Genre();
        genre.setId(rs.getInt("genre_id"));
        genre.setName(rs.getString("genre_name"));
        return new AbstractMap.SimpleEntry<>(filmId, genre);
    }

    private Film mapToFilm(ResultSet rs, int i) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("film_name"));
        film.setDescription(rs.getString("film_description"));
        film.setReleaseDate(rs.getObject("film_release_date", LocalDate.class));
        film.setDuration(rs.getInt("film_duration"));
        MotionPictureAssociation mpa = new MotionPictureAssociation();
        mpa.setName(rs.getString("mpa_name"));
        mpa.setId(rs.getInt("mpa_id"));
        film.setMpa(mpa);
        return film;
    }

    @Override
    public Film getById(int filmId) {
        String sqlQuery = BASE_SELECT + " from FILMS f join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id " +
                "where f.film_id = :filmId";
        Film film = null;
        try {
            film = jdbcTemplate.queryForObject(
                    sqlQuery,
                    new MapSqlParameterSource("filmId", filmId),
                    this::mapToFilm
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
        fillFilms(Collections.singletonList(film));
        return film;
    }

    @Override
    public void addAppraiser(int filmId, int userId) {
        String sqlQuery = "insert into APPRAISERS (user_id, film_id) values (:userId, :filmId)";
        jdbcTemplate.update(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("filmId", filmId)
        );
    }

    @Override
    public boolean isFilmHasAppraiser(int filmId, int userId) {
        String sqlQuery = "select count(1) from APPRAISERS where user_id = :userId and film_id = :filmId";
        Integer count = jdbcTemplate.queryForObject(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("filmId", filmId),
                Integer.class
        );
        return count != null && count > 0;
    }

    @Override
    public void removeAppraiser(int filmId, int userId) {
        String sqlQuery = "delete from APPRAISERS where user_id = :userId and film_id = :filmId";
        jdbcTemplate.update(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("filmId", filmId)
        );
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        String sqlQuery = BASE_SELECT + ",(select count(1) from APPRAISERS a where a.film_id = f.film_id) " +
                "as filmAppraisers from FILMS f join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id " +
                "order by filmAppraisers desc limit :count";
        List<Film> films = jdbcTemplate.query(
                sqlQuery,
                new MapSqlParameterSource("count", count),
                this::mapToFilm);
        fillFilms(films);
        System.out.println("films = " + films);
        return films;
    }

    /**
     * метод получения данных о всех популярных фильмах (по кол-ву лайков, по жанру и по году)
     */
    @Override
    public List<Film> getPopularFilmsByGenreAndYear(int count, int genreId, int year) {
        String sqlQuery = null;
        String genreIdExistQuery = "select count(name) from GENRES where genre_id = :genreId";

        if (genreId == 0 && year == 0) {
            log.info("Запрос на получение списка популярных фильмов count={}", count);
            sqlQuery = BASE_SELECT + ",(select count(1) from APPRAISERS a where a.film_id = f.film_id)" +
                    " as filmAppraisers from FILMS f join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id " +
                    "order by filmAppraisers desc limit :count";
        }

        if (genreId != 0 && year == 0) {
            log.info("Запрос на получение списка популярных фильмов по жанру={} : count={} ", genreId, count);
            if ((jdbcTemplate.queryForObject(genreIdExistQuery,
                    Collections.singletonMap("genreId", genreId), Integer.class) == null) ||
                    jdbcTemplate.queryForObject(genreIdExistQuery,
                            Collections.singletonMap("genreId", genreId), Integer.class) == 0) {
                throw new NotFoundException("Жанра с ID = %s не существует", genreId);
            }
            if ((jdbcTemplate.queryForList(LIKES_EXIST_QUERY, new MapSqlParameterSource(), Integer.class)).isEmpty()) {
                sqlQuery = "SELECT m.*, fg.GENRE_ID " +
                        "FROM FILM_GENRES fg " +
                        "INNER JOIN (" + BASE_SELECT +
                        " from FILMS f join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id) as m " +
                        "ON fg.FILM_ID =m.FILM_ID " +
                        "WHERE fg.GENRE_ID = :genreId " +
                        "ORDER BY m.FILM_ID DESC " +
                        "LIMIT 10";
            } else {
                sqlQuery = "SELECT m.*, fg.GENRE_ID, ab.count " +
                        "FROM FILM_GENRES fg " +
                        "INNER JOIN (SELECT FILM_ID, COUNT(USER_ID) AS count " +
                        "FROM APPRAISERS a " +
                        "GROUP BY FILM_ID) as ab ON fg.FILM_ID =ab.FILM_ID " +
                        "INNER JOIN (" + BASE_SELECT +
                        " from FILMS f join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id) as m " +
                        "ON fg.FILM_ID =m.FILM_ID " +
                        "WHERE fg.GENRE_ID = :genreId " +
                        "ORDER BY ab.count DESC " +
                        "LIMIT :count";
            }
        }

        if (genreId == 0 && year != 0) {
            log.info("Запрос на получение списка популярных фильмов по году={} : count={} ", year, count);
            if ((jdbcTemplate.queryForList(LIKES_EXIST_QUERY, new MapSqlParameterSource(), Integer.class)).isEmpty()) {
                sqlQuery = BASE_SELECT + " from FILMS f join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id= f.mpa_id " +
                        "WHERE EXTRACT(YEAR FROM f.release_date)= :year " +
                        "ORDER BY f.film_id " +
                        "LIMIT :count";
            } else {
                sqlQuery = "SELECT m.*, fg.GENRE_ID, ab.count " +
                        "FROM FILM_GENRES fg " +
                        "INNER JOIN (SELECT FILM_ID, COUNT(USER_ID) AS count " +
                        "FROM APPRAISERS a " +
                        "GROUP BY FILM_ID) as ab ON fg.FILM_ID =ab.FILM_ID " +
                        "INNER JOIN (" + BASE_SELECT +
                        " from FILMS f join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id) as m " +
                        "ON fg.FILM_ID =m.FILM_ID " +
                        "WHERE EXTRACT(YEAR FROM m.film_release_date)= :year " +
                        "ORDER BY m.FILM_ID " +
                        "LIMIT :count";
            }
        }

        if (genreId != 0 && year != 0) {
            log.info("Запрос на получение списка популярных фильмов по году={} и жанру={} : count={}",
                    year, genreId, count);
            if ((jdbcTemplate.queryForObject(genreIdExistQuery,
                    Collections.singletonMap("genreId", genreId), Integer.class) == null) ||
                    jdbcTemplate.queryForObject(genreIdExistQuery,
                            Collections.singletonMap("genreId", genreId), Integer.class) == 0) {
                throw new NotFoundException("Жанра с ID = %s не существует", genreId);
            }
            if ((jdbcTemplate.queryForList(LIKES_EXIST_QUERY, new MapSqlParameterSource(), Integer.class)).isEmpty()) {
                sqlQuery = "SELECT m.*, fg.GENRE_ID " +
                        "FROM FILM_GENRES fg " +
                        "INNER JOIN (" + BASE_SELECT +
                        " from FILMS f join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id) as m " +
                        "ON fg.FILM_ID =m.FILM_ID " +
                        "WHERE fg.GENRE_ID = :genreId AND EXTRACT(YEAR FROM m.film_release_date)= :year " +
                        "ORDER BY m.FILM_ID, m.film_release_date DESC " +
                        "LIMIT :count";
            } else {
                sqlQuery = "SELECT m.*, fg.GENRE_ID, ab.count " +
                        "FROM FILM_GENRES fg " +
                        "INNER JOIN (SELECT FILM_ID, COUNT(USER_ID) AS count " +
                        "FROM APPRAISERS a " +
                        "GROUP BY FILM_ID) as ab ON fg.FILM_ID =ab.FILM_ID " +
                        "INNER JOIN (" + BASE_SELECT +
                        " from FILMS f join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id) as m " +
                        "ON fg.FILM_ID =m.FILM_ID " +
                        "WHERE fg.GENRE_ID = :genreId AND EXTRACT(YEAR FROM m.film_release_date)= :year " +
                        "ORDER BY ab.count DESC " +
                        "LIMIT :count";
            }
        }
        List<Film> films = jdbcTemplate.query(
                sqlQuery, new MapSqlParameterSource()
                        .addValue("genreId", genreId)
                        .addValue("year", year)
                        .addValue("count", count),
                this::mapToFilm
        );
        fillFilms(films);
        return films;
    }
}