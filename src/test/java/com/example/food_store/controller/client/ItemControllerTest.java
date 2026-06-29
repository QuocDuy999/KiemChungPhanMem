package com.example.food_store.controller.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import org.springframework.ui.Model;

import com.example.food_store.domain.*;

import com.example.food_store.service.impl.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock private ProductService productService;
    @Mock private UserService userService;
    @Mock private VNPAYService vnpayService;
    @Mock private CartService cartService;

    @Mock private Model model;
    @Mock private HttpServletRequest request;
    @Mock private HttpSession session;

    @InjectMocks
    private ItemController itemController;

    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        cart = new Cart();
        cart.setCartDetails(new ArrayList<>());
    }

    // ================= PRODUCT DETAIL =================
    @Test
    void getProductPage_ShouldReturnView() {

        Product p = new Product();
        p.setId(1L);

        when(productService.fetchProductById(1L))
                .thenReturn(Optional.of(p));

        when(productService.getQuantitybyType(anyString()))
                .thenReturn(10L);

        String view = itemController.getProductPage(model, 1L);

        assertEquals("client/product/detail", view);
    }

    // ================= CART =================
    @Test
    void getCartPage_ShouldReturnView() {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        when(productService.fetchByUser(any(User.class)))
                .thenReturn(cart);

        String view = itemController.getCartPage(model, request);

        assertEquals("client/cart/show", view);
    }

    // ================= CHECKOUT =================
    @Test
    void getCheckoutPage_ShouldReturnView() {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        when(userService.getUserById(1L)).thenReturn(user);
        when(productService.fetchByUser(any(User.class))).thenReturn(cart);

        String view = itemController.getCheckOutPage(
                model, request, 1000, "HN"
        );

        assertEquals("client/cart/checkout", view);
    }

    // ================= ADD TO CART =================
    @Test
    void addToCart_ShouldRedirect() {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("test@gmail.com");

        String view = itemController.addProductToCart(1L, request);

        assertEquals("redirect:/products", view);
    }

    // ================= DELETE CART =================
    @Test
    void deleteCart_ShouldRedirect() {

        when(request.getSession(false)).thenReturn(session);

        String view = itemController.deleteCartDetail(1L, request);

        assertEquals("redirect:/cart", view);
    }

    // ================= PLACE ORDER =================
    @Test
    void placeOrder_ShouldRedirect() throws Exception {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        when(vnpayService.getIpAddress(any())).thenReturn("127.0.0.1");
        when(vnpayService.generateVNPayURL(anyDouble(), anyString(), anyString()))
                .thenReturn("http://vnpay");

        String view = itemController.handlePlaceOrder(
                request,
                "A",
                "B",
                "C",
                "ONLINE",
                "1000"
        );

        assertEquals("redirect:http://vnpay", view);
    }

    // ================= ADD FROM DETAIL =================
    @Test
    void addFromDetail_ShouldRedirect() {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("test@gmail.com");

        String view = itemController.handleAddProductFromViewDetail(
                1L, 2L, request
        );

        assertEquals("redirect:/product/1", view);
    }
}