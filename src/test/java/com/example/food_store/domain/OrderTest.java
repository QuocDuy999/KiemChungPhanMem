package com.example.food_store.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
    }

    @Test
    void testGettersAndSetters() {
        User user = new User();
        user.setId(1L);
        
        order.setId(1L);
        order.setTotalPrice(150000.0);
        order.setReceiverName("Nguyen Van A");
        order.setReceiverAddress("Ho Chi Minh");
        order.setReceiverPhone("0123456789");
        order.setStatus("PENDING");
        order.setPaymentRef("REF123");
        order.setPaymentStatus("PAID");
        order.setPaymentMethod("VNPay");
        order.setUser(user);
        order.setOrderDetails(new ArrayList<>());

        assertEquals(1L, order.getId());
        assertEquals(150000.0, order.getTotalPrice());
        assertEquals("Nguyen Van A", order.getReceiverName());
        assertEquals("Ho Chi Minh", order.getReceiverAddress());
        assertEquals("0123456789", order.getReceiverPhone());
        assertEquals("PENDING", order.getStatus());
        assertEquals("REF123", order.getPaymentRef());
        assertEquals("PAID", order.getPaymentStatus());
        assertEquals("VNPay", order.getPaymentMethod());
        assertNotNull(order.getUser());
        assertNotNull(order.getOrderDetails());
    }

    @Test
    void testToString() {
        order.setId(10L);
        order.setTotalPrice(250000.0);

        String expected = "Order [id=10, totalPrice=250000.0]";
        assertEquals(expected, order.toString());
    }
}