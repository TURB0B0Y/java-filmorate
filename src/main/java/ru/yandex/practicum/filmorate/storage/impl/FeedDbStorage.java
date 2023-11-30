package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Set<Feed> getFeeds(int id) {
        String sqlQuery = "select * from FEEDS where user_id = ?";

        Set<Feed> feeds = new LinkedHashSet<>(jdbcTemplate.query(sqlQuery, FeedDbStorage::makeFeed, id));
        return feeds;
    }

    @Override
    public void createFeed(int userId, int entityId, EventType eventType, Operation operation, long timeStamp) {
        String sqlQuery = "insert into FEEDS(user_id, entity_id, event_type, operation, times) " +
                "values(?, ?, ?, ?, ?)";

        jdbcTemplate.update(sqlQuery, userId, entityId, eventType.toString(), operation.toString(), timeStamp);
    }

    static Feed makeFeed(ResultSet rs, int rowNum) throws SQLException {
        return Feed.builder()
                .eventId(rs.getInt("feed_id"))
                .userId(rs.getInt("user_id"))
                .entityId(rs.getInt("entity_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(Operation.valueOf(rs.getString("operation")))
                .timestamp(rs.getLong("times"))
                .build();
    }
}
