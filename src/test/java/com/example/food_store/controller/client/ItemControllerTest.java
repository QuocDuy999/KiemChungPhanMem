package com.example.food_store.controller.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

import com.example.food_store.domain.Cart;
import com.example.food_store.domain.CartDetail;
import com.example.food_store.domain.Product;
import com.example.food_store.domain.User;
import com.example.food_store.domain.dto.ProductCriteriaDTO;
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

        when(productService.fetchProductById(1L)).thenReturn(Optional.of(product));
        when(productService.getQuantitybyType(anyString())).thenReturn(10L);

        String view = itemController.getProductPage(model, 1L);

        assertEquals("client/product/detail", view);
        verify(model).addAttribute("prd", product);
    }

    @Test
    void getProductPage_ProductNotFound_ShouldRedirectHome() {
        when(productService.fetchProductById(1L)).thenReturn(Optional.empty());

        String view = itemController.getProductPage(model, 1L);

        assertEquals("redirect:/", view);
    }

    // ================= SHOW PRODUCTS (PAGINATION & SORTING) =================

    @Test
    void getProductPage_ProductsList_InvalidPage_ShouldReturnNotMatch() {
        ProductCriteriaDTO dto = new ProductCriteriaDTO();
        when(request.getParameter("page")).thenReturn("invalid_page_string");

        String view = itemController.getProductPage(model, dto, request);

        assertEquals("not-match", view);
    }

    @Test
    void getProductPage_ProductsList_ValidPage_ShouldReturnView() {
        ProductCriteriaDTO dto = new ProductCriteriaDTO();
        when(request.getParameter("page")).thenReturn("1");

        Page<Product> pageData = new PageImpl<>(new ArrayList<>());
        when(productService.fetchProductsWithSpec(any(Pageable.class), eq(dto)))
                .thenReturn(pageData);

        when(request.getQueryString()).thenReturn("page=1&sort=gia-tang-dan");
        
        String view = itemController.getProductPage(model, dto, request);

        assertEquals("client/product/show", view);
        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute(eq("queryString"), anyString());
    }

    @Test
    void getProductPage_ProductsList_SortAscending_ShouldApplySort() {
        ProductCriteriaDTO dto = new ProductCriteriaDTO();
        dto.setSort(Optional.of("gia-tang-dan"));
        when(request.getParameter("page")).thenReturn("1");

        Page<Product> pageData = new PageImpl<>(new ArrayList<>());
        when(productService.fetchProductsWithSpec(any(), any())).thenReturn(pageData);

        itemController.getProductPage(model, dto, request);

        verify(productService).fetchProductsWithSpec(
                argThat(pageable -> pageable.getSort().toString().contains("ASC")), 
                eq(dto)
        );
    }

    @Test
    void getProductPage_ProductsList_SortDescending_ShouldApplySort() {
        ProductCriteriaDTO dto = new ProductCriteriaDTO();
        dto.setSort(Optional.of("gia-giam-dan"));
        when(request.getParameter("page")).thenReturn("1");

        Page<Product> pageData = new PageImpl<>(new ArrayList<>());
        when(productService.fetchProductsWithSpec(any(), any())).thenReturn(pageData);

        itemController.getProductPage(model, dto, request);

        verify(productService).fetchProductsWithSpec(
                argThat(pageable -> pageable.getSort().toString().contains("DESC")), 
                eq(dto)
        );
    }

    // ================= CART =================

    @Test
    void getCartPage_ShouldReturnView() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        when(productService.fetchByUser(any(User.class))).thenReturn(cart);

        String view = itemController.getCartPage(model, request);

        assertEquals("client/cart/show", view);
        verify(model).addAttribute("cart", cart);
        verify(model).addAttribute(eq("cartDetails"), any());
        verify(model).addAttribute("totalPrice", 0.0);
    }

    @Test
    void addProductToCart_ShouldRedirect() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("test@gmail.com");

        String view = itemController.addProductToCart(1L, request);

        assertEquals("redirect:/products", view);
        verify(productService).handleAddProductToCart("test@gmail.com", 1L, session, 1);
    }

    @Test
    void addFromDetail_ShouldRedirect() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("test@gmail.com");

        String view = itemController.handleAddProductFromViewDetail(1L, 2L, request);

        assertEquals("redirect:/product/1", view);
        verify(productService).handleAddProductToCart("test@gmail.com", 1L, session, 2L);
    }

    @Test
    void deleteCart_ShouldRedirect() {
        when(request.getSession(false)).thenReturn(session);

        String view = itemController.deleteCartDetail(1L, request);

        assertEquals("redirect:/cart", view);
        verify(productService).handleRemoveCartDetail(1L, session);
    }

    @Test
    void calculateFee_ShouldUpdateCartDetailQuantities() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        Cart currentCart = new Cart();
        CartDetail currentDetail = new CartDetail();
        currentDetail.setId(10L);
        currentDetail.setQuantity(1);
        List<CartDetail> currentDetails = new ArrayList<>();
        currentDetails.add(currentDetail);
        currentCart.setCartDetails(currentDetails);

        when(cartService.findByUser(any(User.class))).thenReturn(currentCart);

        Cart viewCart = new Cart();
        CartDetail viewDetail = new CartDetail();
        viewDetail.setId(10L);
        viewDetail.setQuantity(5);
        List<CartDetail> viewDetails = new ArrayList<>();
        viewDetails.add(viewDetail);
        viewCart.setCartDetails(viewDetails);

        String view = itemController.calculateFee(viewCart, request);

        assertEquals("client/cart/calculate-fee", view);
        assertEquals(5, currentDetail.getQuantity()); 
        verify(cartService).saveCart(currentCart);
    }

    // ================= CHECKOUT =================

    @Test
    void getCheckoutPage_ShouldReturnView() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        when(userService.getUserById(1L)).thenReturn(user);
        when(productService.fetchByUser(any(User.class))).thenReturn(cart);

        String view = itemController.getCheckOutPage(model, request, 1000, "HN");

        assertEquals("client/cart/checkout", view);
        verify(model).addAttribute("cost", 1000);
        verify(model).addAttribute("detailAddress", "HN");
    }

    @Test
    void confirmCheckout_ShouldUpdateCartAndRedirect() {
        Cart checkoutCart = new Cart();
        List<CartDetail> details = new ArrayList<>();
        checkoutCart.setCartDetails(details);

        String view = itemController.getCheckOutPage(checkoutCart);

        assertEquals("redirect:/checkout", view);
        verify(productService).handleUpdateCartBeforeCheckout(details);
    }

    // ================= PLACE ORDER =================

    @Test
    void placeOrder_Online_ShouldRedirectVNPay() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        when(vnpayService.getIpAddress(any())).thenReturn("127.0.0.1");
        when(vnpayService.generateVNPayURL(anyDouble(), anyString(), anyString())).thenReturn("http://vnpay");

        String view = itemController.handlePlaceOrder(
                request, "A", "B", "C", "ONLINE", "1000");

        assertEquals("redirect:http://vnpay", view);
        verify(productService).handlePlaceOrder(
                any(User.class), eq(session), eq("A"), eq("B"), eq("C"), eq("ONLINE"), anyString(), eq(1000.0));
    }

    @Test
    void placeOrder_COD_ShouldRedirectAfterOrder() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);

        String view = itemController.handlePlaceOrder(
                request, "A", "B", "C", "COD", "1000");

        assertEquals("redirect:/afterOrder", view);
        verify(productService).handlePlaceOrder(
                any(User.class), eq(session), eq("A"), eq("B"), eq("C"), eq("COD"), anyString(), eq(1000.0));
        verify(vnpayService, never()).generateVNPayURL(anyDouble(), anyString(), anyString());
    }

    // ================= AFTER ORDER =================

    @Test
    void getAfterOrderPage_WithoutVNPay_ShouldSendEmailAndReturnView() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);

        String view = itemController.getAfterOrderPage(request, Optional.empty(), Optional.empty());

        assertEquals("client/cart/after-order", view);
        verify(emailProducer).sendEmailToQueue(any());
        verify(productService, never()).updatePaymentStatus(anyString(), anyString());
    }

    @Test
    void getAfterOrderPage_WithVNPaySuccess_ShouldUpdatePaymentStatusSuccess() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);

        String view = itemController.getAfterOrderPage(request, Optional.of("00"), Optional.of("txn123"));

        assertEquals("client/cart/after-order", view);
        verify(emailProducer).sendEmailToQueue(any());
        verify(productService).updatePaymentStatus("txn123", "Thanh toán thành công");
    }

    @Test
    void getAfterOrderPage_WithVNPayFail_ShouldUpdatePaymentStatusFail() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("id")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);

        String view = itemController.getAfterOrderPage(request, Optional.of("24"), Optional.of("txn123"));

        assertEquals("client/cart/after-order", view);
        verify(emailProducer).sendEmailToQueue(any());
        verify(productService).updatePaymentStatus("txn123", "Thanh toán thất bại");
    }
}