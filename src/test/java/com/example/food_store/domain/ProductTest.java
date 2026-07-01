package com.example.food_store.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
    }

    @Test
    void testGettersAndSetters() {
        product.setId(1L);
        product.setName("Rau Sạch");
        product.setPrice(10000.0);
        product.setImage("image.png");
        product.setDetailDesc("Chi tiet");
        product.setShortDesc("Ngan gon");
        product.setQuantity(50L);
        product.setSource("Da Lat");
        product.setUnit("Kg");
        product.setTarget("Moi nguoi");
        product.setType("Rau");
        product.setCustomerTarget("Ca nhan");

        assertEquals(1L, product.getId());
        assertEquals("Rau Sạch", product.getName());
        assertEquals(10000.0, product.getPrice());
        assertEquals("image.png", product.getImage());
        assertEquals("Chi tiet", product.getDetailDesc());
        assertEquals("Ngan gon", product.getShortDesc());
        assertEquals(50L, product.getQuantity());
        assertEquals("Da Lat", product.getSource());
        assertEquals("Kg", product.getUnit());
        assertEquals("Moi nguoi", product.getTarget());
        assertEquals("Rau", product.getType());
        assertEquals("Ca nhan", product.getCustomerTarget());
    }

    @Test
    void testToString() {
        product.setId(5L);
        product.setName("ProductA");
        product.setPrice(200.0);
        product.setImage("img.jpg");
        product.setDetailDesc("Detail");
        product.setShortDesc("Short");
        product.setQuantity(10L);
        product.setTarget("TargetA");

        // Chuỗi expected này được lấy chính xác từ cấu trúc toString hiện tại của Product.java
        String expected = "Product [id=5, name=ProductA, price=200.0, image=img.jpg, detailDesc=Detail, shortDesc=Short, quantity=10, =, target=TargetA]";
        
        assertEquals(expected, product.toString());
    }
}