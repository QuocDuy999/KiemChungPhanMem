package com.example.food_store.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.data.domain.PageRequest;

import com.example.food_store.domain.Order;
import com.example.food_store.domain.OrderDetail;
import com.example.food_store.domain.User;
import com.example.food_store.repository.OrderDetailRepository;
import com.example.food_store.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private User user;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1);

        order = new Order();
        order.setId(1);
        order.setStatus("PENDING");
        order.setUser(user);
    }

    @Test
    void testFetchAllOrders() {

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Order> page =
                new PageImpl<>(Collections.singletonList(order));

        when(orderRepository.findAll(pageable))
                .thenReturn(page);

        Page<Order> result =
                orderService.fetchAllOrders(pageable);

        assertEquals(1, result.getTotalElements());

        verify(orderRepository)
                .findAll(pageable);
    }

    @Test
    void testFetchOrderById_Found() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        Optional<Order> result =
                orderService.fetchOrderById(1L);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void testFetchOrderById_NotFound() {

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Order> result =
                orderService.fetchOrderById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void testFetchOrderByUser() {

        List<Order> orders =
                Collections.singletonList(order);

        when(orderRepository.findByUser(user))
                .thenReturn(orders);

        List<Order> result =
                orderService.fetchOrderByUser(user);

        assertEquals(1, result.size());

        verify(orderRepository)
                .findByUser(user);
    }

    @Test
    void testDeleteById() {

        OrderDetail detail1 = new OrderDetail();
        detail1.setId(10);

        OrderDetail detail2 = new OrderDetail();
        detail2.setId(20);

        order.setOrderDetails(
                Arrays.asList(detail1, detail2));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        orderService.deleteById(1L);

        verify(orderDetailRepository)
                .deleteById(10L);

        verify(orderDetailRepository)
                .deleteById(20L);

        verify(orderRepository)
                .deleteById(1L);
    }

    @Test
    void testDeleteById_OrderNotFound() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        orderService.deleteById(1L);

        verify(orderRepository)
                .deleteById(1L);

        verify(orderDetailRepository, never())
                .deleteById(anyLong());
    }

    @Test
    void testUpdateOrder_Success() {

        Order currentOrder = new Order();
        currentOrder.setId(1);
        currentOrder.setStatus("PENDING");

        Order newOrder = new Order();
        newOrder.setId(1);
        newOrder.setStatus("COMPLETED");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(currentOrder));

        orderService.updateOrder(newOrder);

        assertEquals(
                "COMPLETED",
                currentOrder.getStatus());

        verify(orderRepository)
                .save(currentOrder);
    }

    @Test
    void testUpdateOrder_OrderNotFound() {

        Order newOrder = new Order();
        newOrder.setId(1);
        newOrder.setStatus("COMPLETED");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        orderService.updateOrder(newOrder);

        verify(orderRepository, never())
                .save(any(Order.class));
    }
    @Test
    void testFetchOrderByUser_EmptyList() {

        when(orderRepository.findByUser(user))
                .thenReturn(Collections.emptyList());

        List<Order> result =
                orderService.fetchOrderByUser(user);

        assertEquals(0, result.size());

        verify(orderRepository)
                .findByUser(user);
    }

    @Test
    void testUpdateOrder_ToCancelled() {

        Order currentOrder = new Order();
        currentOrder.setId(1);
        currentOrder.setStatus("PENDING");

        Order newOrder = new Order();
        newOrder.setId(1);
        newOrder.setStatus("CANCELLED");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(currentOrder));

        orderService.updateOrder(newOrder);

        assertEquals(
                "CANCELLED",
                currentOrder.getStatus());

        verify(orderRepository)
                .save(currentOrder);
    }
    
}
