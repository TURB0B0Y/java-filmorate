package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String BASE_SELECT = "select" +
            " f.film_id as film_id," +
            " f.name as film_name," +
            " f.description as film_description," +
            " f.release_date as film_release_date," +
            " f.duration as film_duration," +
            " f.rate as film_rate," +
            " mpa.mpa_id as mpa_id," +
            " mpa.name as mpa_name," +
            " g.genre_id as genre_id," +
            " g.name as genre_name," +
            " a.user_id as appraiser_id";
    private static final String BASE_JOIN =
            " from FILMS f" +
            " left join APPRAISERS a on f.film_id = a.film_id" +
            " left join FILM_GENRES fg on fg.film_id = f.film_id left join GENRES g on fg.genre_id = g.genre_id" +
            " left join MOTION_PICTURE_ASSOCIATIONS mpa on mpa.mpa_id = f.mpa_id";
    private static final String BASE_QUERY = BASE_SELECT + BASE_JOIN;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlQuery = "insert into FILMS(name, description, release_date, duration, mpa_id, rate) values (:name, :description, :releaseDate, :duration, :mpaId, :rate)";
        jdbcTemplate.update(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("name", film.getName())
                        .addValue("description", film.getDescription())
                        .addValue("releaseDate", film.getReleaseDate())
                        .addValue("duration", film.getDuration())
                        .addValue("rate", film.getRate())
                        .addValue("mpaId", film.getMpa().getId()),
                keyHolder
        );
        film.setId(keyHolder.getKey().intValue());
        if (film.getGenres() == null)
            return;
        StringBuilder insertGenresBuilder = new StringBuilder();
        for (Genre genre : film.getGenres()) {
            insertGenresBuilder.append(String.format("insert into FILM_GENRES (film_id, genre_id) VALUES (:filmId, %s);%n", genre.getId()));
        }
        jdbcTemplate.update(insertGenresBuilder.toString(), new MapSqlParameterSource("filmId", film.getId()));
    }

    @Override
    public void editFilm(Film film) {
        String sqlQuery = "update FILMS set name = :name, description = :description, release_date = :releaseDate, duration = :duration, mpa_id = :mpaId, rate = :rate where film_id = :filmId";
        jdbcTemplate.update(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("name", film.getName())
                        .addValue("description", film.getDescription())
                        .addValue("releaseDate", film.getReleaseDate())
                        .addValue("duration", film.getDuration())
                        .addValue("mpaId", film.getMpa().getId())
                        .addValue("rate", film.getRate())
                        .addValue("filmId", film.getId())
                );

        String getFilmGenresQuery = "select film_genre_id, genre_id from FILM_GENRES where film_id = :filmId";
        List<AbstractMap.SimpleEntry<Integer, Integer>> oldGenres = jdbcTemplate.query(
                getFilmGenresQuery,
                new MapSqlParameterSource("filmId", film.getId()),
                (rs, rowNum) -> new AbstractMap.SimpleEntry<>(
                        rs.getInt("film_genre_id"),
                        rs.getInt("genre_id")
                )
        );
        Set<Integer> actualGenreIds = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        oldGenres.removeIf(oldGenreItem -> actualGenreIds.remove(oldGenreItem.getValue()));
        StringBuilder updateGenresQueryBuilder = new StringBuilder("delete from FILM_GENRES where film_genre_id in (:removeIds);");
        for (Integer genreId : actualGenreIds) {
            updateGenresQueryBuilder.append(String.format(
                    "insert into FILM_GENRES (film_id, genre_id) values (:filmId, %s);",
                    genreId
            ));
        }
        jdbcTemplate.update(
                updateGenresQueryBuilder.toString(),
                new MapSqlParameterSource("filmId", film.getId())
                        .addValue("removeIds", oldGenres.stream().map(AbstractMap.SimpleEntry::getKey)
                                .collect(Collectors.toList()))
        );
    }

    @Override
    public Collection<Film> getAll() {
        return jdbcTemplate.query(BASE_QUERY, new FilmListResultSetExtractor());
    }

    private static class FilmListResultSetExtractor implements ResultSetExtractor<List<Film>> {

        @Override
        public List<Film> extractData(ResultSet rs) throws SQLException, DataAccessException {
            FilmResultSetExtractor filmResultSetExtractor = new FilmResultSetExtractor();
            List<Film> films = new LinkedList<>();
            int lastFilmId = -1;
            while (rs.next()) {
                int currentFilmId = rs.getInt("film_id");
                if (lastFilmId != currentFilmId) {
                    if (lastFilmId > 0) {
                        films.add(filmResultSetExtractor.get());
                        filmResultSetExtractor.clear();
                    }
                    lastFilmId = currentFilmId;
                }
                filmResultSetExtractor.step(rs);
            }
            if (lastFilmId > 0) {
                films.add(filmResultSetExtractor.get());
                filmResultSetExtractor.clear();
            }
            return films;
        }
    }

    private static class FilmResultSetExtractor implements ResultSetExtractor<Film> {

        private Film film = null;
        private final Map<Integer, Genre> genres = new HashMap<>();
        private final Set<Integer> appraisers = new HashSet<>();

        @Override
        public Film extractData(ResultSet rs) throws SQLException, DataAccessException {
            while (rs.next()) {
                step(rs);
            }
            return get();
        }

        public void clear() {
            film = null;
            genres.clear();
            appraisers.clear();
        }

        public Film get() {
            if (film == null)
                return null;
            film.setAppraisers(appraisers);
            film.setGenres(new LinkedList<>(genres.values()));
            return film;
        }

        public void step(ResultSet rs) throws SQLException {
            if (film == null)
                film = createFilm(rs);
            int genreId = rs.getInt("genre_id");
            if (!rs.wasNull())
                genres.computeIfAbsent(genreId, key -> createGenre(rs, genreId));
            int appraiser = rs.getInt("appraiser_id");
            if (!rs.wasNull())
                appraisers.add(appraiser);
        }

        private static Genre createGenre(ResultSet rs, Integer genreId) {
            try {
                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName(rs.getString("genre_name"));
                return genre;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static Film createFilm(ResultSet rs) throws SQLException {
            Film film;
            film = new Film();
            film.setId(rs.getInt("film_id"));
            film.setRate(rs.getDouble("film_rate"));
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
    }

    @Override
    public Film getById(int filmId) {
        String sqlQuery = BASE_QUERY + " where f.film_id = :filmId";
        return jdbcTemplate.query(
                sqlQuery,
                new MapSqlParameterSource("filmId", filmId),
                new FilmResultSetExtractor()
        );
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        String sqlQuery = BASE_SELECT + ",(select count(1) from APPRAISERS a where a.film_id = f.film_id) as filmAppraisers" + BASE_JOIN + "  order by filmAppraisers desc limit :count";
        return jdbcTemplate.query(sqlQuery, new MapSqlParameterSource("count", count), new FilmListResultSetExtractor());
    }

    @Override
    public void addAppraiser(Film film, User user) {
        String sqlQuery = "insert into APPRAISERS (user_id, film_id) values (:userId, :filmId)";
        jdbcTemplate.update(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("userId", user.getId())
                        .addValue("filmId", film.getId())
        );
    }

    @Override
    public boolean isFilmHasAppraiser(Film film, User user) {
        String sqlQuery = "select count(1) from APPRAISERS where user_id = :userId and film_id = :filmId";
        Integer count = jdbcTemplate.queryForObject(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("userId", user.getId())
                        .addValue("filmId", film.getId()),
                Integer.class
        );
        return count != null && count > 0;
    }

    @Override
    public void removeAppraiser(Film film, User user) {
        String sqlQuery = "delete from APPRAISERS where user_id = :userId and film_id = :filmId";
        jdbcTemplate.update(
                sqlQuery,
                new MapSqlParameterSource()
                        .addValue("userId", user.getId())
                        .addValue("filmId", film.getId())
        );
    }
}
