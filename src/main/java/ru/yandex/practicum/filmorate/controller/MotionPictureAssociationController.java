package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.service.MotionPictureAssociationService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MotionPictureAssociationController {

    private final MotionPictureAssociationService motionPictureAssociationService;

    @GetMapping
    public Collection<MotionPictureAssociation> getAll() {
        return motionPictureAssociationService.getAll();
    }

    @GetMapping("/{mpaId}")
    public MotionPictureAssociation getById(@Valid @PathVariable @Positive int mpaId) {
        return motionPictureAssociationService.getById(mpaId);
    }
}
