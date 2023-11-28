package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Review save(Review review) {
        System.out.println("START CREATE OR UPDATE REVIEW " + review);
        MapSqlParameterSource source = new MapSqlParameterSource("userId", review.getUserId())
                .addValue("filmId", review.getFilmId())
                .addValue("content", review.getContent())
                .addValue("reviewId", review.getReviewId())
                .addValue("isPositive", review.getIsPositive() ? 1 : 0);
        // Если id указан, создание записи, иначе обновление
        if (review.getReviewId() == null) {
            if (jdbcTemplate.queryForObject("select count(1) as c from USERS where user_id = :userId", new MapSqlParameterSource("userId", review.getUserId()), (rs, rowNum) -> rs.getInt("c")) == 0)
                throw new NotFoundException("Пользоваетль с id %s не найден", review.getUserId());
            if (jdbcTemplate.queryForObject("select count(1) as c from FILMS where film_id = :filmId", new MapSqlParameterSource("filmId", review.getFilmId()), (rs, rowNum) -> rs.getInt("c")) == 0)
                throw new NotFoundException("Фильм с id %s не найден", review.getFilmId());
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update("insert into REVIEWS (content, is_positive, user_id, film_id) VALUES (:content, :isPositive, :userId, :filmId)", source, keyHolder);
            review.setReviewId(keyHolder.getKey().intValue());
            System.out.println("CREATE REVIEW WITH ID " + review.getReviewId());
        } else {
            jdbcTemplate.update("UPDATE REVIEWS SET content = :content, is_positive = :isPositive where review_id = :reviewId", source);
            return findById(review.getReviewId()).get();
        }
        return review;
    }

    @Override
    public void deleteById(Integer reviewId) {
        jdbcTemplate.update(
                "DELETE FROM REVIEWS where review_id = :reviewId",
                new MapSqlParameterSource("reviewId", reviewId)
        );
    }

    @Override
    public Optional<Review> findById(Integer reviewId) {
        // SELECT SUM суммирует все лайки, тк для дизлайка значение rate -1, а для лайка 1, в итоге получается рейтинг отзыва
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT *, (SELECT SUM(rl.rate) FROM REVIEW_LIKES rl where rl.review_id = r.review_id) as useful FROM REVIEWS r where r.review_id = :reviewId",
                    new MapSqlParameterSource("reviewId", reviewId),
                    this::mapToReview
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Review mapToReview(ResultSet resultSet, int i) throws SQLException {
        Review review = new Review();
        review.setReviewId(resultSet.getInt("review_id"));
        review.setContent(resultSet.getString("content"));
        // Тип отзыва BIT, хранит 0 или 1, преобразуем в true/false
        review.setIsPositive(resultSet.getByte("is_positive") == 1);
        review.setUserId(resultSet.getInt("user_id"));
        review.setFilmId(resultSet.getInt("film_id"));
        review.setUseful(resultSet.getInt("useful"));
        return review;
    }

    @Override
    public List<Review> findAllByFilm(Integer filmId, Integer count) {
        // SELECT SUM суммирует все лайки, тк для дизлайка значение rate -1, а для лайка 1, в итоге получается рейтинг отзыва
        return jdbcTemplate.query(
                "SELECT *, (SELECT IFNULL (SUM(rl.rate), 0) FROM REVIEW_LIKES rl where rl.review_id = r.review_id) AS useful FROM REVIEWS r where r.film_id = :filmId ORDER BY useful desc limit :limit",
                new MapSqlParameterSource("limit", count).addValue("filmId", filmId),
                this::mapToReview
        );
    }

    @Override
    public List<Review> findAll(Integer count) {
        // SELECT SUM суммирует все лайки, тк для дизлайка значение rate -1, а для лайка 1, в итоге получается рейтинг отзыва
        return jdbcTemplate.query(
                "SELECT *, (SELECT IFNULL (SUM(rl.rate), 0)  FROM REVIEW_LIKES rl where rl.review_id = r.review_id) as useful FROM REVIEWS r ORDER BY useful desc limit :limit",
                new MapSqlParameterSource("limit", count),
                this::mapToReview
        );
    }

    @Override
    public void addLike(Integer reviewId, Integer userId, int rate) {
        jdbcTemplate.update(
                "INSERT INTO REVIEW_LIKES (review_id, user_id, rate) VALUES (:reviewId, :userId, :rate)",
                new MapSqlParameterSource("reviewId", reviewId).addValue("userId", userId)
                        .addValue("rate", rate)
        );
    }

    @Override
    public void deleteLike(Integer reviewId, Integer userId, int rate) {
        jdbcTemplate.update(
                "DELETE FROM REVIEW_LIKES where review_id = :reviewId and user_id = :userId and rate = :rate",
                new MapSqlParameterSource("reviewId", reviewId).addValue("userId", userId)
                        .addValue("rate", rate)
        );
    }
}
