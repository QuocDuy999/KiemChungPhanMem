package com.example.food_store.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

import com.example.food_store.domain.Order;
import com.example.food_store.domain.OrderDetail;
import com.example.food_store.service.impl.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private Model model;

    @InjectMocks
    private OrderController orderController;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
    }

    @Test
    void getDashboard_ShouldReturnOrderPage() {

        Page<Order> page = new PageImpl<>(List.of(order));

        when(orderService.fetchAllOrders(any(Pageable.class)))
                .thenReturn(page);

        String view = orderController.getDashboard(model, Optional.of("1"));

        assertEquals("admin/order/show", view);

        verify(orderService, times(1)).fetchAllOrders(any(Pageable.class));
        verify(model, times(1)).addAttribute(eq("orders"), any());
        verify(model, times(1)).addAttribute(eq("currentPage"), eq(1));
    }

    @Test
    void getOrderDetail_ShouldReturnDetailPage() {

        Order mockOrder = mock(Order.class);
        OrderDetail detail = mock(OrderDetail.class);

        when(orderService.fetchOrderById(1L))
                .thenReturn(Optional.of(mockOrder));

        when(mockOrder.getOrderDetails())
                .thenReturn(List.of(detail));

        String view = orderController.getMethodName(1L, model);

        assertEquals("admin/order/detail", view);

        verify(model).addAttribute(eq("id"), eq(1L));
        verify(model).addAttribute(eq("orderDetails"), any());
        verify(model).addAttribute(eq("order"), eq(mockOrder));
    }

    @Test
    void getDeletePage_ShouldReturnDeleteView() {

        String view = orderController.getDeleteOrderPage(model, 1L);

        assertEquals("admin/order/delete", view);

        verify(model).addAttribute(eq("id"), eq(1L));
        verify(model).addAttribute(eq("newOrder"), any(Order.class));
    }

    @Test
    void getUpdatePage_ShouldReturnUpdateView() {

        when(orderService.fetchOrderById(1L))
                .thenReturn(Optional.of(order));

        String view = orderController.getUpdateOrderPage(model, 1L);

        assertEquals("admin/order/update", view);

        verify(model).addAttribute(eq("newOrder"), eq(order));
    }

    @Test
    void postDeleteOrder_ShouldCallServiceAndRedirect() {

        String view = orderController.postDeleteOrder(order);

        assertEquals("redirect:/admin/order", view);

        verify(orderService, times(1)).deleteById(1L);
    }

    @Test
    void postUpdateOrder_ShouldCallServiceAndRedirect() {

        String view = orderController.handleUpdateOrder(order);

        assertEquals("redirect:/admin/order", view);

        verify(orderService, times(1)).updateOrder(order);
    }
}