package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;
import ru.yandex.practicum.filmorate.storage.impl.MotionPictureAssociationDbStorage;

import java.util.Collection;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@ActiveProfiles("test")
public class MotionPictureAssociationDbStorageTest {

    private final MotionPictureAssociationDbStorage motionPictureAssociationDbStorage;

    @Autowired
    public MotionPictureAssociationDbStorageTest(NamedParameterJdbcTemplate jdbcTemplate) {
        this.motionPictureAssociationDbStorage = new MotionPictureAssociationDbStorage(jdbcTemplate);
    }


    @Test
    public void testFindAll() {
        Collection<MotionPictureAssociation> mpas = motionPictureAssociationDbStorage.findAll();
        assertThat(mpas).isNotNull();
        assertThat(mpas.isEmpty()).isEqualTo(false);
    }
}
