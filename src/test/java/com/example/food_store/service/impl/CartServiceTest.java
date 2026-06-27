package com.example.food_store.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.food_store.domain.Cart;
import com.example.food_store.domain.User;
import com.example.food_store.repository.CartRepository;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

@Mock
private CartRepository cartRepository;

@InjectMocks
private CartService cartService;

private Cart cart;
private User user;

@BeforeEach
void setUp() {

    user = new User();
    user.setId(1);

    cart = new Cart();
    cart.setId(1);
    cart.setSum(5);
    cart.setUser(user);
}

@Test
void testGetCartByID_Success() {

    when(cartRepository.findById(1L))
            .thenReturn(Optional.of(cart));

    Cart result = cartService.getCartByID(1L);

    assertNotNull(result);
    assertEquals(1, result.getId());
    assertEquals(5, result.getSum());
}

@Test
void testGetCartByID_NotFound() {

    when(cartRepository.findById(2L))
            .thenReturn(Optional.empty());

    Cart result = cartService.getCartByID(2L);

    assertNull(result);
}

@Test
void testFindByUser() {

    when(cartRepository.findByUser(user))
            .thenReturn(cart);

    Cart result = cartService.findByUser(user);

    assertNotNull(result);
    assertEquals(user, result.getUser());
}

@Test
void testSaveCart() {

    when(cartRepository.save(cart))
            .thenReturn(cart);

    Cart result = cartService.saveCart(cart);

    assertNotNull(result);
    assertEquals(5, result.getSum());
}
@Test
    void testSaveCart_Boundary_ZeroSum() {

        cart.setSum(0); 

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result = cartService.saveCart(cart);

        assertNotNull(result);
        assertEquals(0, result.getSum());
    }
@Test
    void testSaveCart_UpdateNewSum() {

        cart.setSum(10); 

        when(cartRepository.save(cart))
                .thenReturn(cart);

        Cart result = cartService.saveCart(cart);

        assertNotNull(result);
        assertEquals(10, result.getSum());
    }
}
