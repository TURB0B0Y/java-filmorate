package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;

    private final FeedStorage feedStorage;

    @Transactional
    public Review addReview(Review review) {
        review.setReviewId(null);
        try {
            Review createdReview = reviewStorage.save(review);
            feedStorage.createFeed(review.getUserId(), review.getReviewId(), EventType.REVIEW,
                    Operation.ADD, Instant.now().toEpochMilli());
            return createdReview;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Отзыв уже существует");
        }
    }

    @Transactional
    public Review editReview(Review review) {
        try {
            Review savedReview = reviewStorage.save(review);
            feedStorage.createFeed(savedReview.getUserId(), savedReview.getReviewId(), EventType.REVIEW,
                    Operation.UPDATE, Instant.now().toEpochMilli());
            return savedReview;
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Отзыв id %s не существует, либо Фильм или Пользоваетль не существует",
                    review.getReviewId());
        }
    }

    @Transactional
    public void deleteReview(Integer reviewId) {
        Review review = reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыва %s не существует, удаление невозможно", reviewId));
        reviewStorage.deleteById(reviewId);
        feedStorage.createFeed(review.getUserId(), reviewId, EventType.REVIEW, Operation.REMOVE, Instant.now().toEpochMilli());
    }

    @Transactional(readOnly = true)
    public Review getReviewById(Integer reviewId) {
        return reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id %s не найден", reviewId));
    }

    @Transactional(readOnly = true)
    public List<Review> getAllByFilm(Integer filmId, Integer count) {
        return reviewStorage.findAllByFilm(filmId, count);
    }

    @Transactional(readOnly = true)
    public List<Review> getAll(Integer count) {
        return reviewStorage.findAll(count);
    }

    @Transactional
    public void addLike(Integer reviewId, Integer userId) {
        try {
            reviewStorage.addLike(reviewId, userId, 1);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Отзыв уже оценен");
        }
    }

    @Transactional
    public void addDislike(Integer reviewId, Integer userId) {
        try {
            reviewStorage.addLike(reviewId, userId, -1);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Отзыв уже оценен");
        }
    }

    @Transactional
    public void deleteLike(Integer reviewId, Integer userId) {
        reviewStorage.deleteLike(reviewId, userId, 1);
    }

    @Transactional
    public void deleteDislike(Integer reviewId, Integer userId) {
        reviewStorage.deleteLike(reviewId, userId, -1);
    }
}
