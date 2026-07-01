package com.example.food_store.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import com.example.food_store.domain.dto.ProductCriteriaDTO;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.example.food_store.domain.Cart;
import com.example.food_store.domain.CartDetail;
import com.example.food_store.domain.Order;
import com.example.food_store.domain.OrderDetail;
import com.example.food_store.domain.Product;
import com.example.food_store.domain.User;
import com.example.food_store.domain.dto.ProductCriteriaDTO;
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
@Test
void testRemoveCartDetail_NotFound() {

    when(cartDetailRepository.findById(99L))
            .thenReturn(Optional.empty());

    productService.handleRemoveCartDetail(99L, session);

    verify(cartRepository, never())
            .save(any(Cart.class));

    verify(cartRepository, never())
            .deleteById(anyLong());

    verify(session, never())
            .setAttribute(anyString(), any());
}


@Test
void testBuildPriceSpecification() {

    List<String> prices = Arrays.asList(
            "duoi-100-nghin",
            "100-150-nghin",
            "150-200-nghin",
            "tren-200-nghin"
    );

    Specification<Product> spec =
            productService.buildPriceSpecification(prices);

    assertNotNull(spec);
}

@Test
void testFetchAllProductsToHomePage() {

    when(productRepository.findAll())
            .thenReturn(Collections.singletonList(product));

    List<Product> result =
            productService.fetchAllProductsToHomePage();

    assertEquals(1, result.size());

    verify(productRepository).findAll();
}

@Test
void testDeleteProductById() {

    productService.deleteProductById(1L);

    verify(productRepository).deleteById(1L);
}

@Test
void testFetchByUser() {

    when(cartRepository.findByUser(user))
            .thenReturn(cart);

    Cart result =
            productService.fetchByUser(user);

    assertEquals(cart, result);

    verify(cartRepository).findByUser(user);
}

@Test
void testFetchOrders() {

    Order order = new Order();

    when(orderRepository.findAll())
            .thenReturn(Collections.singletonList(order));

    List<Order> result =
            productService.fetchOrders();

    assertEquals(1, result.size());

    verify(orderRepository).findAll();
}

@Test
void testHandleAddProductToCart_UserNull() {

    when(userService.getUserByEmail("abc@gmail.com"))
            .thenReturn(null);

    productService.handleAddProductToCart(
            "abc@gmail.com",
            1,
            session,
            2);

    verify(cartRepository, never()).save(any());
}

@Test
void testHandleAddProductToCart_ProductNotFound() {

    when(userService.getUserByEmail("abc@gmail.com"))
            .thenReturn(user);

    when(cartRepository.findByUser(user))
            .thenReturn(cart);

    when(productRepository.findById(99L))
            .thenReturn(Optional.empty());

    productService.handleAddProductToCart(
            "abc@gmail.com",
            99,
            session,
            1);

    verify(cartDetailRepository, never()).save(any());
}

@Test
void testHandlePlaceOrder_CartNull() {

    when(cartRepository.findByUser(user))
            .thenReturn(null);

    productService.handlePlaceOrder(
            user,
            session,
            "A",
            "B",
            "0123",
            "COD",
            "uuid",
            100);

    verify(orderRepository, never()).save(any());
}

@Test
void testHandlePlaceOrder_CartDetailNull() {

    cart.setCartDetails(null);

    when(cartRepository.findByUser(user))
            .thenReturn(cart);

    productService.handlePlaceOrder(
            user,
            session,
            "A",
            "B",
            "0123",
            "COD",
            "uuid",
            100);

    verify(orderRepository, never()).save(any());
}
// --- Các test bổ sung cho fetchProducts và fetchProductsWithSpec ---

    @Test
    void testFetchProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<Product> page = new PageImpl<>(Collections.singletonList(product));
        
        when(productRepository.findAll(pageable)).thenReturn(page);
        
        org.springframework.data.domain.Page<Product> result = productService.fetchProducts(pageable);
        
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void testFetchProductsWithSpec_NullCriteria() {
        Pageable pageable = PageRequest.of(0, 10);
        ProductCriteriaDTO dto = new ProductCriteriaDTO(); // Tất cả các field đều null
        PageImpl<Product> page = new PageImpl<>(Collections.singletonList(product));
        
        when(productRepository.findAll(pageable)).thenReturn(page);
        
        org.springframework.data.domain.Page<Product> result = productService.fetchProductsWithSpec(pageable, dto);
        
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void testFetchProductsWithSpec_WithCriteria() {
        Pageable pageable = PageRequest.of(0, 10);
        ProductCriteriaDTO dto = new ProductCriteriaDTO();
        dto.setText(Optional.of("Pizza"));
        dto.setTarget(Optional.of(Arrays.asList("Adult")));
        dto.setType(Optional.of(Arrays.asList("FastFood")));
        dto.setCustomertarget(Optional.of(Arrays.asList("VIP")));
        dto.setPrice(Optional.of(Arrays.asList("100-150-nghin")));

        PageImpl<Product> page = new PageImpl<>(Collections.singletonList(product));
        
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        org.springframework.data.domain.Page<Product> result = productService.fetchProductsWithSpec(pageable, dto);
        
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    // --- Các test bổ sung cho các nhánh điều kiện (branch) còn thiếu ---

    @Test
    void testHandleUpdateCartBeforeCheckout_NotFound() {
        CartDetail updateDetail = new CartDetail();
        updateDetail.setId(99L);
        updateDetail.setQuantity(5);

        when(cartDetailRepository.findById(99L)).thenReturn(Optional.empty());

        productService.handleUpdateCartBeforeCheckout(Collections.singletonList(updateDetail));

        // Đảm bảo không có lời gọi save nào được thực hiện khi không tìm thấy CartDetail
        verify(cartDetailRepository, never()).save(any());
    }

    @Test
    void testHandlePlaceOrder_NotCOD() {
        // Test nhánh điều kiện khi paymentMethod không phải là COD (vd: VNPAY)
        CartDetail detail = new CartDetail();
        detail.setId(1);
        detail.setProduct(product);
        detail.setPrice(100000);
        detail.setQuantity(2);

        cart.setCartDetails(Collections.singletonList(detail));

        when(cartRepository.findByUser(user)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        productService.handlePlaceOrder(
                user,
                session,
                "Cloud",
                "Tien Giang",
                "0123456789",
                "VNPAY", // Khác COD
                "UUID-VNPAY-123",
                200000
        );

        // Sử dụng ArgumentCaptor để kiểm tra chính xác tham số Order được truyền vào save()
        org.mockito.ArgumentCaptor<Order> orderCaptor = org.mockito.ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        // Kiểm tra xem paymentRef có được set thành uuid thay vì "UNKNOWN" hay không
        assertEquals("UUID-VNPAY-123", savedOrder.getPaymentRef());
        verify(orderDetailRepository).save(any(OrderDetail.class));
        verify(cartRepository).deleteById(cart.getId());
        verify(session).setAttribute("sum", 0);
    }
    
    @Test
    void testBuildPriceSpecification_UnknownPrice() {
        // Kiểm tra nhánh default trong switch-case của buildPriceSpecification
        List<String> prices = Arrays.asList("gia-khong-hop-le");
        
        Specification<Product> spec = productService.buildPriceSpecification(prices);
        
        assertNotNull(spec);
    }
}
