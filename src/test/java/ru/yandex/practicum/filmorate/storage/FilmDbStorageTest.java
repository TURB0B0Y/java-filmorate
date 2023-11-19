package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.util.RandomUtils;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@ActiveProfiles("test")
public class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @Autowired
    public FilmDbStorageTest(NamedParameterJdbcTemplate jdbcTemplate) {
        this.filmDbStorage = new FilmDbStorage(jdbcTemplate);
        this.userDbStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    public void testFindFilmById() {
        Film film = RandomUtils.getRandomFilm();
        filmDbStorage.addFilm(film);
        Film filmFromDB = filmDbStorage.getById(film.getId());
        assertThat(filmFromDB).isNotNull().usingRecursiveComparison().ignoringFields("mpa.name")
                .isEqualTo(film);
    }

    @Test
    public void testEditFilm() {
        Film film = RandomUtils.getRandomFilm();
        filmDbStorage.addFilm(film);
        Film edit = RandomUtils.getRandomFilm();
        edit.setId(film.getId());
        filmDbStorage.editFilm(edit);
        Film filmFromDB = filmDbStorage.getById(film.getId());
        assertThat(filmFromDB).isNotNull().usingRecursiveComparison().ignoringFields("mpa.name")
                .isEqualTo(edit);
    }

    @Test
    public void testFindAllFilms() {
        Film film = RandomUtils.getRandomFilm();
        filmDbStorage.addFilm(film);
        Collection<Film> films = filmDbStorage.getAll();
        assertThat(films).isNotNull();
        assertThat(films.isEmpty()).isEqualTo(false);
    }

    @Test
    public void testAddAppraiser() {
        Film film = RandomUtils.getRandomFilm();
        filmDbStorage.addFilm(film);
        User user = RandomUtils.getRandomUser();
        userDbStorage.addUser(user);
        filmDbStorage.addAppraiser(film.getId(), user.getId());
        Film filmFromDB = filmDbStorage.getById(film.getId());
        assertThat(filmFromDB).isNotNull();
        assertThat(filmFromDB.getAppraisers()).isNotNull();
        assertThat(filmFromDB.getAppraisers().isEmpty()).isEqualTo(false);
        assertThat(filmFromDB.getAppraisers().size()).isEqualTo(1);
        assertThat(filmFromDB.getAppraisers()).isEqualTo(Collections.singleton(user.getId()));
    }

    @Test
    public void testRemoveAppraiser() {
        Film film = RandomUtils.getRandomFilm();
        filmDbStorage.addFilm(film);
        User user = RandomUtils.getRandomUser();
        userDbStorage.addUser(user);
        filmDbStorage.addAppraiser(film.getId(), user.getId());
        filmDbStorage.removeAppraiser(film.getId(), user.getId());
        Film filmFromDB = filmDbStorage.getById(film.getId());
        assertThat(filmFromDB).isNotNull();
        assertThat(filmFromDB.getAppraisers()).isNotNull();
        assertThat(filmFromDB.getAppraisers().isEmpty()).isEqualTo(true);
    }

    @Test
    public void testGetPopularFilms() {
        for (int i = 0; i < 10; i++) {
            filmDbStorage.addFilm(RandomUtils.getRandomFilm());
        }
        Film film = RandomUtils.getRandomFilm();
        filmDbStorage.addFilm(film);
        for (int i = 0; i < 10; i++) {
            User user = RandomUtils.getRandomUser();
            userDbStorage.addUser(user);
            filmDbStorage.addAppraiser(film.getId(), user.getId());
            film.getAppraisers().add(user.getId());
        }
        Collection<Film> popularFilms = filmDbStorage.getPopularFilms(1);
        assertThat(popularFilms).isNotNull();
        assertThat(popularFilms.isEmpty()).isEqualTo(false);
        assertThat(popularFilms).usingRecursiveComparison().ignoringFields("mpa.name")
                .isEqualTo(Collections.singletonList(film));
    }

}
