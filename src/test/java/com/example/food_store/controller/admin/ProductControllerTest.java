package com.example.food_store.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.example.food_store.domain.Product;
import com.example.food_store.service.impl.ProductService;
import com.example.food_store.service.impl.UploadService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private UploadService uploadService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private ProductController productController;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
    }

    // =========================
    // GET LIST PRODUCT
    // =========================
    @Test
    void getProduct_ShouldReturnView() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productService.fetchProducts(any(Pageable.class))).thenReturn(page);

        String view = productController.getProduct(model, Optional.of("1"));

        assertEquals("admin/product/show", view);

        verify(productService).fetchProducts(any(Pageable.class));
        verify(model).addAttribute(eq("products"), any());
        verify(model).addAttribute(eq("currentPage"), eq(1));
    }

    // =========================
    // CREATE PAGE
    // =========================
    @Test
    void getCreatePage_ShouldReturnView() {
        String view = productController.getCreateProductPage(model);

        assertEquals("admin/product/create", view);

        verify(model).addAttribute(eq("newPrd"), any(Product.class));
    }

    // =========================
    // DELETE PAGE
    // =========================
    @Test
    void getDeletePage_ShouldReturnView() {
        String view = productController.getDeleteProductPage(model, 1L);

        assertEquals("admin/product/delete", view);

        verify(model).addAttribute(eq("id"), eq(1L));
        verify(model).addAttribute(eq("newProduct"), any(Product.class));
    }

    // =========================
    // DETAIL PAGE
    // =========================
    @Test
    void getDetailPage_ShouldReturnView() {
        when(productService.fetchProductById(1L)).thenReturn(Optional.of(product));

        String view = productController.getProductDetailPage(model, 1L);

        assertEquals("admin/product/detail", view);

        verify(model).addAttribute(eq("product"), eq(product));
        verify(model).addAttribute(eq("id"), eq(1L));
    }

    // =========================
    // UPDATE PAGE
    // =========================
    @Test
    void getUpdatePage_ShouldReturnView() {
        when(productService.fetchProductById(1L)).thenReturn(Optional.of(product));

        String view = productController.getUpdateProductPage(model, 1L);

        assertEquals("admin/product/update", view);

        verify(model).addAttribute(eq("newProduct"), eq(product));
    }

    // =========================
    // CREATE PRODUCT POST
    // =========================
    @Test
    void createProduct_ShouldRedirect() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(uploadService.handleSaveUploadFile(any(), eq("product"))).thenReturn("img.png");

        String view = productController.createProduct(product, bindingResult, file);

        assertEquals("redirect:/admin/product", view);

        verify(uploadService).handleSaveUploadFile(file, "product");
        verify(productService).createProduct(product);
    }

    // =========================
    // DELETE PRODUCT POST
    // =========================
    @Test
    void deleteProduct_ShouldRedirect() {
        String view = productController.postDeleteProduct(model, product);

        assertEquals("redirect:/admin/product", view);

        verify(productService).deleteProductById(1L);
    }

    // =========================
    // UPDATE PRODUCT POST
    // =========================
    @Test
    void updateProduct_ShouldRedirect() {
        Product existing = new Product();
        existing.setId(1L);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(productService.fetchProductById(1L)).thenReturn(Optional.of(existing));
        when(file.isEmpty()).thenReturn(true);

        String view = productController.handleUpdateProduct(product, bindingResult, file);

        assertEquals("redirect:/admin/product", view);

        verify(productService).fetchProductById(1L);
        verify(productService).createProduct(existing);
    }
}