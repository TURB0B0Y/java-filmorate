package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.SortingFilms;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {

    @Qualifier("directorDbStorage")
    private final DirectorStorage directorStorage;
    private static final String BASE_SELECT = "select" +
            " f.film_id as film_id," +
            " f.name as film_name," +
            " f.description as film_description," +
            " f.release_date as film_release_date," +
            " f.duration as film_duration," +
            " mpa.mpa_id as mpa_id," +
            " mpa.name as mpa_name";
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
        saveDirectorByFilm(film);
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
        saveDirectorByFilm(film);
    }

    private void saveDirectorByFilm(Film data) {
        List<Director> directors = data.getDirectors();
        directorStorage.deleteAllDirectorByFilm(data.getId());
        if (directors != null) {
            for (Director director : directors) {
                directorStorage.createDirectorByFilm(director.getId(), data.getId());
            }
        }
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
        List<Integer> filmIds = films.stream().map(Film::getId).collect(toList());
        List<Map.Entry<Integer, Genre>> filmGenres = jdbcTemplate.query(
                "select fg.film_id as film_id, g.genre_id as genre_id, g.name as genre_name from FILM_GENRES fg " +
                        "join GENRES g on g.genre_id = fg.genre_id where fg.film_id in (:filmIds)",
                new MapSqlParameterSource("filmIds", filmIds),
                this::mapToFilmGenre
        );
        for (Map.Entry<Integer, Genre> entry : filmGenres) {
            Film film = filmsMap.get(entry.getKey());
            if (film.getGenres() == null) {
                film.setGenres(new ArrayList<>());
            }
            film.getGenres().add(entry.getValue());
        }
        List<Map.Entry<Integer, Integer>> filmAppraisers = jdbcTemplate.query(
                "select * from APPRAISERS where film_id in (:filmIds)",
                new MapSqlParameterSource("filmIds", filmIds),
                this::mapToFilmAppraiser
        );
        for (Map.Entry<Integer, Integer> entry : filmAppraisers) {
            Film film = filmsMap.get(entry.getKey());
            if (film.getAppraisers() == null) {
                film.setAppraisers(new HashSet<>());
            }
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
    public Collection<Film> getPopularFilms(int count) {
        String sqlQuery = BASE_SELECT + ",(select count(1) from APPRAISERS a where a.film_id = f.film_id) as filmAppraisers " +
                "from FILMS f join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id " +
                "order by filmAppraisers desc limit :count";
        List<Film> films = jdbcTemplate.query(sqlQuery, new MapSqlParameterSource("count", count), this::mapToFilm);
        fillFilms(films);
        return films;
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
    public List<Film> getSortDirectorsOfFilms(int directorId, SortingFilms sort) {
        String sortName = sort.name();
        if (sort.equals(SortingFilms.YEAR)) {
            sortName = sortName + "S";
        }
        String sqlQuery = "SELECT f.FILM_ID AS ID ," +
                "f.name AS name ," +
                "f.description AS description ," +
                "f.release_date AS years ," +
                "f.duration AS duration ," +
                "f.mpa_id AS mpa_id ," +
                "mpa.name as mpa_name ," +
                "d.director_id AS director_id ," +
                "d.name AS director_name ," +
                "COUNT( DISTINCT a.USER_ID) AS likes " +
                "FROM FILMS f left JOIN APPRAISERS a ON a.FILM_ID = f.film_id " +
                "LEFT join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id " +
                "LEFT JOIN film_directors fd ON f.FILM_ID = fd.FILM_ID " +
                "LEFT JOIN DIRECTORS d ON fd.director_id = d.director_id " +
                "WHERE d.director_id =? " +
                "GROUP BY ID, director_id " +
                "ORDER BY " + sortName + " ASC";

        List<Film> films = jdbcTemplate.getJdbcTemplate().query(sqlQuery, (rs, rowNum) -> Film.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getTimestamp("years").toLocalDateTime().toLocalDate())
                        .duration(rs.getInt("duration"))
                        .mpa(MotionPictureAssociation.builder()
                                .id(rs.getInt("mpa_id"))
                                .name(rs.getString("mpa_name"))
                                .build())
                        .build(),

                directorId
        );
        fillFilms(films);
        return films;
    }

    public List<Film> searchMovieByTitleAndDirector(String query, List<String> by) {
        List<Film> films;
        String querySyntax = "%" + query + "%";
        String sqlLastQuery = "SELECT \n" +
                " f.FILM_ID AS film_id,\n" +
                "\tf.NAME AS film_name,\n" +
                "\tf.DESCRIPTION AS film_description,\n" +
                "\tf.RELEASE_DATE AS film_release_date,\n" +
                "\tf.DURATION AS film_duration,\n" +
                "\tg.NAME AS genres,\n" +
                "\tf.MPA_ID AS mpa_id,\n" +
                "\tMPA.NAME AS mpa_name,\n" +
                "\td.NAME AS directors\n" +
                "FROM FILMS f \n" +
                "LEFT JOIN MOTION_PICTURE_ASSOCIATIONS mpa ON f.MPA_ID = MPA.MPA_ID\n" +
                "LEFT JOIN FILM_DIRECTORS fd ON f.FILM_ID = fd.FILM_ID \n" +
                "LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID \n" +
                "LEFT JOIN APPRAISERS a ON f.FILM_ID = a.FILM_ID\n" +
                "LEFT JOIN FILM_GENRES fg ON f.FILM_ID = fg.FILM_ID \n" +
                "LEFT JOIN GENRES g ON fg.GENRE_ID = g.GENRE_ID ";
        if (by.contains("title") && by.contains("director")) {
            String sqlQuery = sqlLastQuery + " WHERE LOWER(f.NAME)  LIKE LOWER(:title)" +
                    " OR LOWER(d.NAME) LIKE LOWER(:director) " +
                    "ORDER BY a.FILM_ID DESC";
            films = jdbcTemplate.query(sqlQuery, new MapSqlParameterSource()
                            .addValue("title", querySyntax)
                            .addValue("director", querySyntax),
                    this::mapToFilm);
            log.info("Собрали список через поиск размером в {} элемент(ов)", films.size());
        } else if (by.contains("director")) {
            String sqlQuery = sqlLastQuery + "WHERE LOWER(d.NAME) LIKE LOWER( :director) " +
                    "ORDER BY a.FILM_ID DESC";
            films = jdbcTemplate.query(sqlQuery, new MapSqlParameterSource()
                            .addValue("director", querySyntax),
                    this::mapToFilm);
            log.info("Собрали список через поиск размером в {} элемент(ов)", films.size());
        } else if (by.contains("title")) {
            String sqlQuery = sqlLastQuery + "WHERE LOWER(f.NAME)  LIKE LOWER( :title) " +
                    "ORDER BY a.FILM_ID DESC";
            films = jdbcTemplate.query(sqlQuery,
                    new MapSqlParameterSource()
                            .addValue("title", querySyntax),
                    this::mapToFilm);
            log.info("Собрали список через поиск размером в {} элемент(ов)", films.size());
        } else {
            throw new NotFoundException("не верно заданы параметры поиска");
        }
        fillFilms(films);
        return films;
    }


    @Override
    public List<Film> moviesSharedWithFriend(int userId, int friendId) {
        String sqlQuery = BASE_SELECT +
                " FROM FILMS f JOIN MOTION_PICTURE_ASSOCIATIONS mpa ON f.MPA_ID=MPA.MPA_ID " +
                "WHERE FILM_ID IN (SELECT FILM_ID FROM APPRAISERS a2" +
                " WHERE USER_ID = :userId AND FILM_ID IN (SELECT FILM_ID  FROM APPRAISERS a " +
                "WHERE USER_ID = :friendId ORDER BY FILM_ID DESC))";
        List<Film> films = jdbcTemplate.query(sqlQuery,
                new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("friendId", friendId), this::mapToFilm);
        return films;
    }

    @Override
    public void deleteFilmById(int id) {
        String sqlQuery = "delete from FILMS where film_id = :filmId";
        jdbcTemplate.update(sqlQuery, new MapSqlParameterSource().addValue("filmId", id));
    }


}
