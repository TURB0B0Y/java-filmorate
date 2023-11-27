package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.util.Collection;
import java.util.Optional;

public interface MotionPictureAssociationStorage {
    Collection<MotionPictureAssociation> findAll();

    Optional<MotionPictureAssociation> findById(Integer id);
}
