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

import com.example.food_store.domain.Cart;
import com.example.food_store.domain.Product;
import com.example.food_store.domain.User;
import com.example.food_store.messaging.producer.EmailProducer;
import com.example.food_store.service.impl.CartService;
import com.example.food_store.service.impl.ProductService;
import com.example.food_store.service.impl.UserService;
import com.example.food_store.service.impl.VNPAYService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private VNPAYService vnpayService;

    @Mock
    private CartService cartService;

    @Mock
    private EmailProducer emailProducer;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ItemController itemController;

    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setFullName("Test User");

        cart = new Cart();
        cart.setCartDetails(new ArrayList<>());
    }

    // ================= PRODUCT DETAIL =================

    @Test
    void getProductPage_ShouldReturnDetailView() {

        Product product = new Product();
        product.setId(1L);

        when(productService.fetchProductById(1L))
                .thenReturn(Optional.of(product));

        when(productService.getQuantitybyType(anyString()))
                .thenReturn(10L);

        String view = itemController.getProductPage(model, 1L);

        assertEquals("client/product/detail", view);

        verify(model).addAttribute("prd", product);
    }

    @Test
    void getProductPage_ProductNotFound_ShouldRedirectHome() {

        when(productService.fetchProductById(1L))
                .thenReturn(Optional.empty());

        String view = itemController.getProductPage(model, 1L);

        assertEquals("redirect:/", view);
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

        verify(model).addAttribute("cart", cart);
        verify(model).addAttribute(eq("cartDetails"), any());
        verify(model).addAttribute("totalPrice", 0.0);
    }

    // ================= CHECKOUT =================

    @Test
    void getCheckoutPage_ShouldReturnView() {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        when(userService.getUserById(1L)).thenReturn(user);
        when(productService.fetchByUser(any(User.class)))
                .thenReturn(cart);

        String view = itemController.getCheckOutPage(
                model,
                request,
                1000,
                "HN");

        assertEquals("client/cart/checkout", view);

        verify(model).addAttribute("cost", 1000);
        verify(model).addAttribute("detailAddress", "HN");
    }

    // ================= ADD TO CART =================

    @Test
    void addProductToCart_ShouldRedirect() {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email"))
                .thenReturn("test@gmail.com");

        String view = itemController.addProductToCart(1L, request);

        assertEquals("redirect:/products", view);

        verify(productService).handleAddProductToCart(
                "test@gmail.com",
                1L,
                session,
                1);
    }

    // ================= DELETE CART =================

    @Test
    void deleteCart_ShouldRedirect() {

        when(request.getSession(false)).thenReturn(session);

        String view = itemController.deleteCartDetail(1L, request);

        assertEquals("redirect:/cart", view);

        verify(productService)
                .handleRemoveCartDetail(1L, session);
    }

    // ================= PLACE ORDER ONLINE =================

    @Test
    void placeOrder_Online_ShouldRedirectVNPay() throws Exception {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        when(vnpayService.getIpAddress(any()))
                .thenReturn("127.0.0.1");

        when(vnpayService.generateVNPayURL(anyDouble(), anyString(), anyString()))
                .thenReturn("http://vnpay");

        String view = itemController.handlePlaceOrder(
                request,
                "A",
                "B",
                "C",
                "ONLINE",
                "1000");

        assertEquals("redirect:http://vnpay", view);

        verify(productService).handlePlaceOrder(
                any(User.class),
                eq(session),
                eq("A"),
                eq("B"),
                eq("C"),
                eq("ONLINE"),
                anyString(),
                eq(1000.0));
    }

    // ================= PLACE ORDER COD =================

    @Test
    void placeOrder_COD_ShouldRedirectAfterOrder() throws Exception {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        String view = itemController.handlePlaceOrder(
                request,
                "A",
                "B",
                "C",
                "COD",
                "1000");

        assertEquals("redirect:/afterOrder", view);

        verify(productService).handlePlaceOrder(
                any(User.class),
                eq(session),
                eq("A"),
                eq("B"),
                eq("C"),
                eq("COD"),
                anyString(),
                eq(1000.0));

        verify(vnpayService, never())
                .generateVNPayURL(anyDouble(), anyString(), anyString());
    }

    // ================= ADD FROM DETAIL =================

    @Test
    void addFromDetail_ShouldRedirect() {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email"))
                .thenReturn("test@gmail.com");

        String view = itemController.handleAddProductFromViewDetail(
                1L,
                2L,
                request);

        assertEquals("redirect:/product/1", view);

        verify(productService).handleAddProductToCart(
                "test@gmail.com",
                1L,
                session,
                2L);
    }
}