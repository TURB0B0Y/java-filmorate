package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Double rate;
    private Set<Integer> appraisers = new HashSet<>();
    private List<Genre> genres = new LinkedList<>();
    private MotionPictureAssociation mpa;
}
