package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Запрос на создание отзыва {}", review);
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review editReview(@Valid @RequestBody Review review) {
        log.info("Запрос на изменение отзыва {}", review);
        return reviewService.editReview(review);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable Integer reviewId) {
        log.info("Запрос на удаление отзыва {}", reviewId);
        reviewService.deleteReview(reviewId);
    }

    @GetMapping("/{reviewId}")
    public Review getReviewById(@PathVariable Integer reviewId) {
        log.info("Запрос на получение отзыва {}", reviewId);
        return reviewService.getReviewById(reviewId);
    }

    @GetMapping
    public List<Review> getAll(@RequestParam(required = false) Integer filmId, @RequestParam(defaultValue = "10", required = false) @Positive Integer count) {
        log.info("Запрос на получение отзывов film:{} count:{}", filmId, count);
        if (filmId == null)
            return reviewService.getAll(count); // получение всех отзывов
        return reviewService.getAllByFilm(filmId, count); // получение всех отзывов по фильму если id фильма указан
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void addLike(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.info("Запрос на лайк отзыва review:{} user:{}", reviewId, userId);
        reviewService.addLike(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void addDislike(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.info("Запрос на дизлайк отзыва review:{} user:{}", reviewId, userId);
        reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void deleteLike(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.info("Запрос на удаление лайка отзыва review:{} user:{}", reviewId, userId);
        reviewService.deleteLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void deleteDislike(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.info("Запрос на удаление дизлайка отзыва review:{} user:{}", reviewId, userId);
        reviewService.deleteDislike(reviewId, userId);
    }
}
