package com.example.food_store.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class OAuth2ProviderConstantTest {

    @Test
    void testConstructorAndConstants() {
        // Khởi tạo để cover hàm constructor mặc định (đạt 100% coverage cho file)
        OAuth2ProviderConstant oauthConstant = new OAuth2ProviderConstant();
        assertNotNull(oauthConstant);

        // Kiểm tra các giá trị hằng số
        assertEquals("GITHUB", OAuth2ProviderConstant.GITHUB);
        assertEquals("GOOGLE", OAuth2ProviderConstant.GOOGLE);
    }
}