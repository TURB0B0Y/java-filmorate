package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.model.Feed;

import java.time.Instant;
import java.util.Set;

public interface FeedStorage {
    Set<Feed> getFeeds(int id);

    void createFeed(int userId, int entityId, EventType eventType, Operation operation, long timeStamp);
}
