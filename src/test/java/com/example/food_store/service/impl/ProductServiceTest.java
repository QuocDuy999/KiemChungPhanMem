package com.example.food_store.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.food_store.domain.Cart;
import com.example.food_store.domain.CartDetail;
import com.example.food_store.domain.Order;
import com.example.food_store.domain.Product;
import com.example.food_store.domain.User;
import com.example.food_store.repository.CartDetailRepository;
import com.example.food_store.repository.CartRepository;
import com.example.food_store.repository.OrderDetailRepository;
import com.example.food_store.repository.OrderRepository;
import com.example.food_store.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {


@Mock
private CartRepository cartRepository;

@Mock
private CartDetailRepository cartDetailRepository;

@Mock
private ProductRepository productRepository;

@Mock
private UserService userService;

@Mock
private OrderDetailRepository orderDetailRepository;

@Mock
private OrderRepository orderRepository;

@Mock
private HttpSession session;

@InjectMocks
private ProductService productService;

private User user;
private Product product;
private Cart cart;

@BeforeEach
void setUp() {

    user = new User();
    user.setId(1);

    product = new Product();
    product.setId(1);
    product.setName("Pizza");
    product.setPrice(100000);

    cart = new Cart();
    cart.setId(1);
    cart.setUser(user);
    cart.setSum(0);
}

@Test
void testGetQuantityByType() {

    when(productRepository.countByType("Food"))
            .thenReturn(10L);

    long result = productService.getQuantitybyType("Food");

    assertEquals(10L, result);
}

@Test
void testCreateProduct() {

    when(productRepository.save(product))
            .thenReturn(product);

    Product result = productService.createProduct(product);

    assertNotNull(result);
    assertEquals("Pizza", result.getName());
}

@Test
void testFetchProductById_Found() {

    when(productRepository.findById(1L))
            .thenReturn(Optional.of(product));

    Optional<Product> result =
            productService.fetchProductById(1L);

    assertTrue(result.isPresent());
}

@Test
void testFetchProductById_NotFound() {

    when(productRepository.findById(99L))
            .thenReturn(Optional.empty());

    Optional<Product> result =
            productService.fetchProductById(99L);

    assertFalse(result.isPresent());
}

@Test
void testAddProductToCart_NewCart() {

    when(userService.getUserByEmail("test@gmail.com"))
            .thenReturn(user);

    when(cartRepository.findByUser(user))
            .thenReturn(null);

    when(cartRepository.save(any(Cart.class)))
            .thenReturn(cart);

    when(productRepository.findById(1L))
            .thenReturn(Optional.of(product));

    when(cartDetailRepository.findByCartAndProduct(any(), any()))
            .thenReturn(null);

    productService.handleAddProductToCart(
            "test@gmail.com",
            1L,
            session,
            2
    );

    verify(cartDetailRepository, times(1))
            .save(any(CartDetail.class));

    verify(session, times(1))
            .setAttribute("sum", 1);
}

@Test
void testAddProductToCart_ProductAlreadyExists() {

    CartDetail detail = new CartDetail();
    detail.setQuantity(2);

    when(userService.getUserByEmail("test@gmail.com"))
            .thenReturn(user);

    when(cartRepository.findByUser(user))
            .thenReturn(cart);

    when(productRepository.findById(1L))
            .thenReturn(Optional.of(product));

    when(cartDetailRepository.findByCartAndProduct(cart, product))
            .thenReturn(detail);

    productService.handleAddProductToCart(
            "test@gmail.com",
            1L,
            session,
            3
    );

    assertEquals(5, detail.getQuantity());

    verify(cartDetailRepository)
            .save(detail);
}

@Test
void testRemoveCartDetail_WhenCartStillExists() {

    cart.setSum(2);

    CartDetail detail = new CartDetail();
    detail.setId(1);
    detail.setCart(cart);

    when(cartDetailRepository.findById(1L))
            .thenReturn(Optional.of(detail));

    productService.handleRemoveCartDetail(1L, session);

    verify(cartRepository).save(cart);

    verify(session)
            .setAttribute("sum", 1);
}

@Test
void testRemoveCartDetail_DeleteCart() {

    cart.setSum(1);

    CartDetail detail = new CartDetail();
    detail.setId(1);
    detail.setCart(cart);

    when(cartDetailRepository.findById(1L))
            .thenReturn(Optional.of(detail));

    productService.handleRemoveCartDetail(1L, session);

    verify(cartRepository)
            .deleteById(cart.getId());

    verify(session)
            .setAttribute("sum", 0);
}

@Test
void testUpdatePaymentStatus() {

    Order order = new Order();

    when(orderRepository.findByPaymentRef("ABC"))
            .thenReturn(order);

    productService.updatePaymentStatus(
            "ABC",
            "SUCCESS"
    );

    assertEquals(
            "SUCCESS",
            order.getPaymentStatus()
    );
}

@Test
void testCountProduct() {

    when(productRepository.count())
            .thenReturn(50L);

    long result = productService.countProduct();

    assertEquals(50L, result);
}

@Test
void testGetAllNames() {

    Product p1 = new Product();
    p1.setName("Pizza");

    Product p2 = new Product();
    p2.setName("Burger");

    when(productRepository.findAll())
            .thenReturn(Arrays.asList(p1, p2));

    List<String> result =
            productService.getAllNames();

    assertEquals(2, result.size());

    assertTrue(result.contains("\"Pizza\""));
    assertTrue(result.contains("\"Burger\""));
}

@Test
void testPlaceOrder_COD() {

    CartDetail detail = new CartDetail();
    detail.setId(1);
    detail.setProduct(product);
    detail.setPrice(100000);
    detail.setQuantity(2);

    cart.setCartDetails(
            Collections.singletonList(detail)
    );

    when(cartRepository.findByUser(user))
            .thenReturn(cart);

    when(orderRepository.save(any(Order.class)))
            .thenAnswer(i -> i.getArgument(0));

    productService.handlePlaceOrder(
            user,
            session,
            "Cloud",
            "Tien Giang",
            "0123456789",
            "COD",
            "UUID123",
            200000
    );

    verify(orderRepository)
            .save(any(Order.class));

    verify(orderDetailRepository)
            .save(any());

    verify(cartRepository)
            .deleteById(cart.getId());

    verify(session)
            .setAttribute("sum", 0);
}
@Test
void testFetchProductByType() {

    List<Product> products =
            Collections.singletonList(product);

    when(productRepository.findByType("Food"))
            .thenReturn(products);

    List<Product> result =
            productService.fetchProductByType("Food");

    assertEquals(1, result.size());
    assertEquals("Pizza", result.get(0).getName());

    verify(productRepository)
            .findByType("Food");
}

@Test
void testHandleUpdateCartBeforeCheckout() {

    CartDetail updateDetail = new CartDetail();
    updateDetail.setId(1);
    updateDetail.setQuantity(5);

    CartDetail currentDetail = new CartDetail();
    currentDetail.setId(1);
    currentDetail.setQuantity(2);

    when(cartDetailRepository.findById(1L))
            .thenReturn(Optional.of(currentDetail));

    productService.handleUpdateCartBeforeCheckout(
            Collections.singletonList(updateDetail)
    );

    assertEquals(
            5,
            currentDetail.getQuantity()
    );

    verify(cartDetailRepository)
            .save(currentDetail);
}

}
