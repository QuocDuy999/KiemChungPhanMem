package com.example.food_store.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.food_store.config.TestConfig;
import com.example.food_store.domain.Order;
import com.example.food_store.domain.OrderDetail;
import com.example.food_store.domain.Product;
import com.example.food_store.domain.User;

@DataJpaTest
@Import(TestConfig.class)
class OrderDetailRepositoryTest {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User user;
    private Order order;
    private Product product;
    private OrderDetail orderDetail;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("admin@gmail.com");
        user.setPassword("123456");
        user.setFullName("Nguyen Van A");
        user = userRepository.save(user);

        order = new Order();
        order.setPaymentRef("REF123");
        order.setReceiverName("Nguyen Van A");
        order.setReceiverAddress("Ho Chi Minh");
        order.setReceiverPhone("0123456789");
        order.setStatus("PENDING");
        order.setPaymentStatus("PAID");
        order.setPaymentMethod("VNPay");
        order.setTotalPrice(150000.0);
        order.setUser(user);
        order = orderRepository.save(order);

        product = new Product();
        product.setName("ProductA");
        product.setPrice(150000.0);
        product.setDetailDesc("Detail");
        product.setShortDesc("Short");
        product.setQuantity(10L);
        product = productRepository.save(product);
        orderDetail = new OrderDetail();
        orderDetail.setQuantity(1);
        orderDetail.setPrice(150000.0);
        orderDetail.setOrder(order);
        orderDetail.setProduct(product);
        orderDetail = orderDetailRepository.save(orderDetail);
    }

    @Test
    void testFindById_Found() {
        Optional<OrderDetail> result = orderDetailRepository.findById(orderDetail.getId());

        assertTrue(result.isPresent());
        assertEquals(orderDetail.getId(), result.get().getId());
        assertEquals(150000.0, result.get().getPrice());
        assertEquals(order.getId(), result.get().getOrder().getId());
        assertEquals(product.getId(), result.get().getProduct().getId());
    }

    @Test
    void testFindById_NotFound() {
        Optional<OrderDetail> result = orderDetailRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void testSaveOrderDetail_Success() {
        OrderDetail newDetail = new OrderDetail();
        newDetail.setQuantity(2);
        newDetail.setPrice(250000.0);
        newDetail.setOrder(order);
        newDetail.setProduct(product);

        OrderDetail savedDetail = orderDetailRepository.save(newDetail);

        assertNotNull(savedDetail);
        assertNotEquals(0, savedDetail.getId());
        assertEquals(2, savedDetail.getQuantity());
        assertEquals(250000.0, savedDetail.getPrice());
        assertEquals(order.getId(), savedDetail.getOrder().getId());
    }
}
