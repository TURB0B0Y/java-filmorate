package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.storage.MotionPictureAssociationStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MotionPictureAssociationService {
    private final MotionPictureAssociationStorage motionPictureAssociationStorage;

    @Transactional(readOnly = true)
    public Collection<MotionPictureAssociation> getAll() {
        return motionPictureAssociationStorage.findAll();
    }

    @Transactional(readOnly = true)
    public MotionPictureAssociation getById(Integer mpaId) {
        return motionPictureAssociationStorage.findById(mpaId)
                .orElseThrow(() -> new NotFoundException("MPA с id %s не существует", mpaId));
    }
}
