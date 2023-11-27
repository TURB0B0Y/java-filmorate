package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserFriend {
    private Integer userId;
    private Integer friendId;
    private Integer status;

    public UserFriend(Integer userId, Integer friendId) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = 0;
    }
}
