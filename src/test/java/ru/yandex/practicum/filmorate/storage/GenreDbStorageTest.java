package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.impl.GenreDbStorage;
import ru.yandex.practicum.filmorate.util.RandomUtils;

import java.util.Collection;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@ActiveProfiles("test")
public class GenreDbStorageTest {
    private final GenreDbStorage genreDbStorage;

    @Autowired
    public GenreDbStorageTest(NamedParameterJdbcTemplate jdbcTemplate) {
        this.genreDbStorage = new GenreDbStorage(jdbcTemplate);
    }

    @Test
    public void testFindById() {
        Genre genre = RandomUtils.getRandomGenre();
        genreDbStorage.save(genre);
        Genre genreFromDB = genreDbStorage.findById(genre.getId()).get();
        assertThat(genreFromDB).isNotNull();
        assertThat(genreFromDB).isNotNull().usingRecursiveComparison().isEqualTo(genre);
    }

    @Test
    public void testFindAll() {
        Genre genre = RandomUtils.getRandomGenre();
        genreDbStorage.save(genre);
        Collection<Genre> genres = genreDbStorage.findAll();
        assertThat(genres).isNotNull();
        assertThat(genres.isEmpty()).isEqualTo(false);
    }
}
