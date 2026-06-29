package com.example.food_store.controller.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import com.example.food_store.service.impl.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class CartAPITest {

    @Mock
    private ProductService productService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private CartAPI cartAPI;

    private CartRequest cartRequest;

    @BeforeEach
    void setUp() {
        cartRequest = new CartRequest();
        cartRequest.setProductId(1L);
        cartRequest.setQuantity(2L);
    }

    // =========================
    // ADD PRODUCT SUCCESS
    // =========================
    @Test
    void addProductToCart_ShouldReturnSum() {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("test@gmail.com");

        when(session.getAttribute("sum")).thenReturn(5);

        doNothing().when(productService)
                .handleAddProductToCart(anyString(), anyLong(), any(HttpSession.class), anyLong());

        ResponseEntity<Integer> response =
                cartAPI.addProductToCart(cartRequest, request);

        assertEquals(5, response.getBody());

        verify(productService).handleAddProductToCart(
                eq("test@gmail.com"),
                eq(1L),
                eq(session),
                eq(2L)
        );
    }

    // =========================
    // QUANTITY = 0 → AUTO = 1
    // =========================
    @Test
    void addProductToCart_WhenQuantityZero_ShouldBeOne() {

        cartRequest.setQuantity(0);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("test@gmail.com");
        when(session.getAttribute("sum")).thenReturn(3);

        doNothing().when(productService)
                .handleAddProductToCart(anyString(), anyLong(), any(HttpSession.class), anyLong());

        ResponseEntity<Integer> response =
                cartAPI.addProductToCart(cartRequest, request);

        assertEquals(3, response.getBody());

        verify(productService).handleAddProductToCart(
                eq("test@gmail.com"),
                eq(1L),
                eq(session),
                eq(1L)   // QUANTITY FIXED = 1
        );
    }
}