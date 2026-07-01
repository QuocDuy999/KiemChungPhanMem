package com.example.food_store.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaseEntityTest {

    private BaseEntity baseEntity;

    @BeforeEach
    void setUp() {
        // BaseEntity không phải là class abstract nên có thể khởi tạo trực tiếp
        baseEntity = new BaseEntity();
    }

    @Test
    void testGettersAndSetters() {
        baseEntity.setCreateDayTime("2026-07-02 10:00:00");
        baseEntity.setUpdateDayTime("2026-07-02 12:00:00");

        assertEquals("2026-07-02 10:00:00", baseEntity.getCreateDayTime());
        assertEquals("2026-07-02 12:00:00", baseEntity.getUpdateDayTime());
    }

    @Test
    void prePersist_ShouldSetCreateAndUpdateTime() {
        // Act
        baseEntity.prePersist();

        // Assert
        assertNotNull(baseEntity.getCreateDayTime());
        assertNotNull(baseEntity.getUpdateDayTime());
        // Khi persist, thời gian tạo và cập nhật phải giống nhau
        assertEquals(baseEntity.getCreateDayTime(), baseEntity.getUpdateDayTime());
    }

    @Test
    void preUpdate_ShouldSetBoth_WhenCreateTimeIsNull() {
        // Arrange
        baseEntity.setCreateDayTime(null);

        // Act
        baseEntity.preUpdate();

        // Assert
        assertNotNull(baseEntity.getCreateDayTime());
        assertNotNull(baseEntity.getUpdateDayTime());
    }

    @Test
    void preUpdate_ShouldOnlySetUpdateTime_WhenCreateTimeIsNotNull() {
        // Arrange
        String existingCreateTime = "2020-01-01 00:00:00";
        baseEntity.setCreateDayTime(existingCreateTime);

        // Act
        baseEntity.preUpdate();

        // Assert
        assertEquals(existingCreateTime, baseEntity.getCreateDayTime());
        assertNotNull(baseEntity.getUpdateDayTime());
    }
}