package com.example.food_store.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.Model;

import com.example.food_store.service.impl.ProductService;
import com.example.food_store.service.impl.UserService;

@ExtendWith(MockitoExtension.class)
class DashBoardControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private Model model;

    @InjectMocks
    private DashBoardController dashBoardController;

    @BeforeEach
    void setUp() {

        when(userService.countUser()).thenReturn(10L);
        when(userService.countOrder()).thenReturn(20L);
        when(productService.countProduct()).thenReturn(30L);

    }

    @Test
    void getDashBoard_ShouldReturnDashboardView() {

        // Act
        String viewName = dashBoardController.getDashBoard(model);

        // Assert
        assertEquals("admin/dashboard/show", viewName);

        verify(userService, times(1)).countUser();
        verify(userService, times(1)).countOrder();
        verify(productService, times(1)).countProduct();

    }

    @Test
    void getDashBoard_ShouldAddCountUserIntoModel() {

        // Act
        dashBoardController.getDashBoard(model);

        // Assert
        verify(model, times(1))
                .addAttribute(eq("countUser"), eq(10L));

    }
        @Test
    void getDashBoard_ShouldAddCountOrderIntoModel() {

        // Act
        dashBoardController.getDashBoard(model);

        // Assert
        verify(model, times(1))
                .addAttribute(eq("countOrder"), eq(20L));

    }

    @Test
    void getDashBoard_ShouldAddCountProductIntoModel() {

        // Act
        dashBoardController.getDashBoard(model);

        // Assert
        verify(model, times(1))
                .addAttribute(eq("countProduct"), eq(30L));

    }

    @Test
    void getDashBoard_ShouldCallAllServiceMethodsExactlyOnce() {

        // Act
        dashBoardController.getDashBoard(model);

        // Assert
        verify(userService, times(1)).countUser();
        verify(userService, times(1)).countOrder();
        verify(productService, times(1)).countProduct();

    }

}