package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;

import javax.validation.constraints.NotNull;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode
public class Feed {
    int eventId;
    @NotNull
    int userId;
    @NotNull
    int entityId;
    @NotNull
    EventType eventType;
    @NotNull
    Operation operation;
    long timestamp;
}
