package com.example.food_store.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AppConstantTest {

    @Test
    void testConstructorAndConstants() {
        // Khởi tạo để cover hàm constructor mặc định (đạt 100% coverage cho file)
        AppConstant appConstant = new AppConstant();
        assertNotNull(appConstant);

        // Kiểm tra các giá trị chuỗi hằng số
        assertEquals("http://localhost:8080/reset-password?token=", AppConstant.RESET_LINK);
        assertEquals("^[a-zA-Z0-9!#$%&*/=?`{|}]+@[a-zA-Z0-9.-]+$", AppConstant.REGEX_EMAIL);
        assertEquals("email_exchange", AppConstant.EXCHANGE);
        assertEquals("email_routingkey", AppConstant.ROUTING_KEY);
        assertEquals("email_queue", AppConstant.QUEUE);
        assertEquals("foodstore247official@gmail.com", AppConstant.SYSTEM_EMAIL_SENDER);
        assertEquals("/resources/images", AppConstant.LOCAL_PATH);
        
        // Kiểm tra tính hợp lệ của DateTimeFormatter
        assertNotNull(AppConstant.formatter);
        LocalDateTime sampleDate = LocalDateTime.of(2026, 7, 2, 12, 0, 0);
        assertEquals("2026-07-02 12:00:00", sampleDate.format(AppConstant.formatter));
    }
}