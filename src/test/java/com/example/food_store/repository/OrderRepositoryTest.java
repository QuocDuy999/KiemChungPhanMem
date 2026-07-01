package com.example.food_store.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.food_store.config.TestConfig;
import com.example.food_store.domain.Order;
import com.example.food_store.domain.User;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;


    @Test
    void count_ReturnNumberOfOrders() {

        // Arrange
        Order order1 = new Order();
        order1.setPaymentRef("PAY001");
        order1.setReceiverName("User 1");
        order1.setReceiverAddress("HCM");
        order1.setReceiverPhone("0900000001");
        order1.setStatus("PENDING");
        order1.setPaymentStatus("UNPAID");
        order1.setPaymentMethod("COD");
        order1.setTotalPrice(100000);


        Order order2 = new Order();
        order2.setPaymentRef("PAY002");
        order2.setReceiverName("User 2");
        order2.setReceiverAddress("HCM");
        order2.setReceiverPhone("0900000002");
        order2.setStatus("PENDING");
        order2.setPaymentStatus("UNPAID");
        order2.setPaymentMethod("COD");
        order2.setTotalPrice(200000);


        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.flush();


        // Act
        long result = orderRepository.count();


        // Assert
        assertEquals(2, result);
    }


    @Test
    void findByPaymentRef_ReturnOrder_WhenPaymentRefExists() {

        // Arrange
        Order order = new Order();

        order.setPaymentRef("PAY123");
        order.setReceiverName("Duy");
        order.setReceiverAddress("HCM");
        order.setReceiverPhone("0900000000");
        order.setStatus("PENDING");
        order.setPaymentStatus("UNPAID");
        order.setPaymentMethod("COD");
        order.setTotalPrice(50000);


        entityManager.persist(order);
        entityManager.flush();


        // Act
        Order result =
                orderRepository.findByPaymentRef("PAY123");


        // Assert
        assertNotNull(result);
        assertEquals("PAY123", result.getPaymentRef());
    }


    @Test
    void findByPaymentRef_ReturnNull_WhenPaymentRefNotExists() {

        // Act
        Order result =
                orderRepository.findByPaymentRef("NOT_FOUND");


        // Assert
        assertNull(result);
    }


    @Test
    void findByUser_ReturnOrders_WhenUserHasOrders() {

        // Arrange
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("123456");
        user.setFullName("Test User");


        entityManager.persist(user);


        Order order = new Order();

        order.setUser(user);
        order.setPaymentRef("USER_PAY");
        order.setReceiverName("Duy");
        order.setReceiverAddress("HCM");
        order.setReceiverPhone("0900000000");
        order.setStatus("PENDING");
        order.setPaymentStatus("UNPAID");
        order.setPaymentMethod("COD");
        order.setTotalPrice(100000);


        entityManager.persist(order);
        entityManager.flush();


        // Act
        List<Order> result =
                orderRepository.findByUser(user);


        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getId(),
                result.get(0).getUser().getId());
    }


    @Test
    void findByUser_ReturnEmpty_WhenUserHasNoOrders() {

        // Arrange
        User user = new User();

        user.setEmail("empty@gmail.com");
        user.setPassword("123456");
        user.setFullName("Empty User");


        entityManager.persist(user);
        entityManager.flush();


        // Act
        List<Order> result =
                orderRepository.findByUser(user);


        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}