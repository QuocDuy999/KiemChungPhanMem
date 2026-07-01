package com.example.food_store.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.food_store.config.TestConfig;
import com.example.food_store.domain.Cart;
import com.example.food_store.domain.User;

@DataJpaTest
@Import(TestConfig.class)
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Cart cart;


    @BeforeEach
    void setUp() {

        user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("123456");
        user.setFullName("Test User");

        user = userRepository.save(user);


        cart = new Cart();
        cart.setUser(user);
        cart.setSum(0);

        cart = cartRepository.save(cart);
    }


    @Test
    void testFindByUser_Found() {

        Cart result = cartRepository.findByUser(user);

        assertNotNull(result);
        assertEquals(cart.getId(), result.getId());
        assertEquals(user.getId(), result.getUser().getId());
    }


    @Test
    void testFindByUser_NotFound() {

        User otherUser = new User();

        otherUser.setEmail("other@gmail.com");
        otherUser.setPassword("123456");
        otherUser.setFullName("Other User");

        otherUser = userRepository.save(otherUser);


        Cart result = cartRepository.findByUser(otherUser);

        assertNull(result);
    }


    @Test
    void testSaveCart_Success() {

        User newUser = new User();

        newUser.setEmail("save@gmail.com");
        newUser.setPassword("123456");
        newUser.setFullName("Save User");

        newUser = userRepository.save(newUser);


        Cart newCart = new Cart();

        newCart.setUser(newUser);
        newCart.setSum(0);


        Cart savedCart = cartRepository.save(newCart);


        assertNotNull(savedCart);
        assertNotEquals(0, savedCart.getId());
        assertEquals(newUser.getId(), savedCart.getUser().getId());
    }
}