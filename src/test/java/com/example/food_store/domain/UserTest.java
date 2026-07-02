package com.example.food_store.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testGettersAndSetters() {
      
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_ADMIN"); 

        Cart cart = new Cart();
        cart.setId(1L);

        user.setId(1L);
        user.setEmail("admin@gmail.com");
        user.setPassword("123456");       
        user.setFullName("Nguyen Van A"); 
        user.setAddress("Ho Chi Minh");  
        user.setPhone("0123456789");     
        user.setAvatar("image.png");
        user.setProvider("LOCAL");
        user.setRole(role);
        user.setOrders(new ArrayList<>());
        user.setCart(cart);

        assertEquals(1L, user.getId());
        assertEquals("admin@gmail.com", user.getEmail());
        assertEquals("123456", user.getPassword());
        assertEquals("Nguyen Van A", user.getFullName());
        assertEquals("Ho Chi Minh", user.getAddress());
        assertEquals("0123456789", user.getPhone());
        assertEquals("image.png", user.getAvatar());
        assertEquals("LOCAL", user.getProvider());
        assertEquals(role, user.getRole());
        assertNotNull(user.getOrders());
        assertEquals(cart, user.getCart());
    }

    @Test
    void testPrePersist_Behavior_Logic() {
       
        user.setProvider(null);
        user.prePersist();
        assertEquals("LOCAL", user.getProvider());

      
        user.setProvider("VNPAY");
        user.prePersist();
        assertEquals("VNPAY", user.getProvider());
    }

    @Test
    void testUser_Boundary_Fields_NullAndEmpty() {
       
        user.setEmail("");
        user.setPassword(null);
        user.setFullName("");

        assertEquals("", user.getEmail());
        assertNull(user.getPassword());
        assertEquals("", user.getFullName());
    }

    @Test
    void testUser_Boundary_InvalidID() {
      
        user.setId(-1L);
        assertEquals(-1L, user.getId());

        user.setId(0L);
        assertEquals(0L, user.getId());
    }
}
