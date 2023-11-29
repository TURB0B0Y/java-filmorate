CREATE TABLE IF NOT EXISTS USERS (
    user_id     INT             GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email       VARCHAR(320)    NOT NULL,
    login       VARCHAR(20)     NOT NULL,
    name        VARCHAR(50)     NOT NULL,
    birthday    DATE            NOT NULL
);

CREATE TABLE IF NOT EXISTS MOTION_PICTURE_ASSOCIATIONS (
   mpa_id    INT          GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
   name      VARCHAR(5)   NOT NULL
);

CREATE TABLE IF NOT EXISTS FILMS (
    film_id         INT             GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    description     VARCHAR(255)    NOT NULL,
    release_date    DATE            NOT NULL,
    duration        INT             NOT NULL,
    mpa_id          INT             NOT NULL,
    CONSTRAINT FILMS_MPA_FK FOREIGN KEY (mpa_id) REFERENCES MOTION_PICTURE_ASSOCIATIONS
    ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS USER_FRIENDS (
    user_id             INT     NOT NULL,
    friend_id           INT     NOT NULL,
    status              INT     NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT USER_FRIENDS_USER_FK FOREIGN KEY (user_id) REFERENCES USERS
    ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT USER_FRIENDS_FRIEND_FK FOREIGN KEY (friend_id) REFERENCES USERS
    ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE IF NOT EXISTS APPRAISERS (
    user_id         INT     NOT NULL,
    film_id         INT     NOT NULL,
    PRIMARY KEY (user_id, film_id),
    CONSTRAINT APPRAISERS_USER_FK FOREIGN KEY (user_id) REFERENCES USERS
    ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT APPRAISERS_FILM_FK FOREIGN KEY (film_id) REFERENCES FILMS
    ON DELETE CASCADE ON UPDATE CASCADE
);



CREATE TABLE IF NOT EXISTS GENRES (
    genre_id    INT         GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name        VARCHAR(50) NOT NULL
);


CREATE TABLE IF NOT EXISTS FILM_GENRES (
    film_id         INT     NOT NULL,
    genre_id        INT     NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    CONSTRAINT FILM_GENRES_FILM_FK FOREIGN KEY (film_id) REFERENCES FILMS
    ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT FILM_GENRES_GENRE_FK FOREIGN KEY (genre_id) REFERENCES GENRES
    ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS REVIEWS (
   review_id        INT     GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
   content          TEXT    NOT NULL,
   is_positive      BIT     NOT NULL,
   user_id          INT     NOT NULL,
   film_id          INT     NOT NULL,
   UNIQUE (film_id, user_id),
   CONSTRAINT REVIEWS_FILM_FK FOREIGN KEY (film_id) REFERENCES FILMS
       ON DELETE CASCADE ON UPDATE CASCADE,
   CONSTRAINT REVIEWS_USER_FK FOREIGN KEY (user_id) REFERENCES USERS
       ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS REVIEW_LIKES (
    review_id   INT     NOT NULL,
    user_id     INT     NOT NULL,
    rate        INT     NOT NULL,
    PRIMARY KEY (review_id, user_id),
    CONSTRAINT REVIEW_LIKES_REVIEW_FK FOREIGN KEY (review_id) REFERENCES REVIEWS
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT REVIEW_LIKES_USER_FK FOREIGN KEY (user_id) REFERENCES USERS
        ON DELETE CASCADE ON UPDATE CASCADE
  );
CREATE TABLE IF NOT EXISTS directors (
    director_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name varchar(150)
);

CREATE TABLE IF NOT EXISTS film_directors (
    director_id integer REFERENCES directors(director_id) ON DELETE CASCADE,
    film_id integer REFERENCES films(film_id) ON DELETE CASCADE,
    PRIMARY KEY (director_id,film_id )
);