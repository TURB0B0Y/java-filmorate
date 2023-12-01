package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    /**
     * Сохранение отзыв
     * При reviewId == null добавление записи в базу
     * Иначе обновление существующей записи
     * @param review модель отзыва
     * @return сохраненный отзыв
     */
    Review save(Review review);

    void deleteById(Integer reviewId);

    Optional<Review> findById(Integer reviewId);

    List<Review> findAllByFilm(Integer filmId, Integer count);

    List<Review> findAll(Integer count);

    /**
     * Добавление лайка
     * @param reviewId - id отзыва
     * @param userId - id пользователя
     * @param rate - Показатель лайк/дизлайк, для лайка значение 1, для дизлайка -1
     */
    void addLike(Integer reviewId, Integer userId, int rate);

    /**
     * Удаление лайка
     * @param reviewId - id отзыва
     * @param userId - id пользователя
     * @param rate - Показатель лайк/дизлайк, для лайка значение 1, для дизлайка -1
     */
    void deleteLike(Integer reviewId, Integer userId, int rate);
}
