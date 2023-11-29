--INSERT INTO MOTION_PICTURE_ASSOCIATIONS (name) VALUES ('G');
--INSERT INTO MOTION_PICTURE_ASSOCIATIONS (name) VALUES ('PG');
--INSERT INTO MOTION_PICTURE_ASSOCIATIONS (name) VALUES ('PG-13');
--INSERT INTO MOTION_PICTURE_ASSOCIATIONS (name) VALUES ('R');
--INSERT INTO MOTION_PICTURE_ASSOCIATIONS (name) VALUES ('NC-17');
--INSERT INTO GENRES (name) VALUES ('Комедия');
--INSERT INTO GENRES (name) VALUES ('Драма');
--INSERT INTO GENRES (name) VALUES ('Мультфильм');
--INSERT INTO GENRES (name) VALUES ('Триллер');
--INSERT INTO GENRES (name) VALUES ('Документальный');
--INSERT INTO GENRES (name) VALUES ('Боевик');
merge into MOTION_PICTURE_ASSOCIATIONS (MPA_id, name)
values (1, 'G'),
       (2, 'PG'),
       (3, 'PG-13'),
       (4, 'R'),
       (5, 'NC-17');

merge into GENRES(genre_id, name)
values (1, 'Комедия'),
       (2, 'Драма'),
       (3, 'Мультфильм'),
       (4, 'Триллер'),
       (5, 'Документальный'),
       (6, 'Боевик');