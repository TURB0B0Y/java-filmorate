package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    public static String getRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ThreadLocalRandom.current().nextInt(97, 122));
        }
        return sb.toString();
    }

    public static int getRandomIntNumber(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public static User getRandomUser() {
        return new User(
                getRandomString(10) + "@email.ru",
                getRandomString(10),
                getRandomString(10),
                LocalDate.of(
                        getRandomIntNumber(1990, 2025),
                        getRandomIntNumber(1, 12),
                        getRandomIntNumber(1, 28)
                )
        );
    }

    public static Film getRandomFilm() {
        Film film = new Film();
        film.setName(getRandomString(10));
        film.setDescription(getRandomString(10));
        film.setReleaseDate(LocalDate.of(
                getRandomIntNumber(1990, 2025),
                getRandomIntNumber(1, 12),
                getRandomIntNumber(1, 28)
        ));
        film.setDuration(getRandomIntNumber(0, 300));
        MotionPictureAssociation mpa = new MotionPictureAssociation();
        mpa.setId(getRandomIntNumber(1, 5));
        film.setMpa(mpa);
        return film;
    }

    public static Genre getRandomGenre() {
        Genre genre = new Genre();
        genre.setName(getRandomString(10));
        return genre;
    }

    public static MotionPictureAssociation getRandomMPA() {
        MotionPictureAssociation mpa = new MotionPictureAssociation();
        mpa.setName(getRandomString(10));
        return mpa;
    }
}
