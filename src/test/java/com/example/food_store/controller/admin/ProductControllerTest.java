
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

        when(productService.fetchProducts(any(Pageable.class)))
                .thenReturn(page);

        String view = productController.getProduct(model, Optional.of("1"));

        assertEquals("admin/product/show", view);

        verify(productService).fetchProducts(any(Pageable.class));
        verify(model).addAttribute(eq("products"), any());
        verify(model).addAttribute("currentPage", 1);
        verify(model).addAttribute("totalPages", page.getTotalPages());
    }

    @Test
    void getProduct_PageEmpty_ShouldReturnView() {

        Page<Product> page = new PageImpl<>(List.of(product));

        when(productService.fetchProducts(any(Pageable.class)))
                .thenReturn(page);

        String view = productController.getProduct(model, Optional.empty());

        assertEquals("admin/product/show", view);

        verify(productService).fetchProducts(any(Pageable.class));
    }

    @Test
    void getProduct_InvalidPage_ShouldReturnNotMatch() {

        String view = productController.getProduct(model, Optional.of("abc"));

        assertEquals("not-match", view);

        verify(model).addAttribute(eq("errorMessage"), anyString());

        verify(productService, never()).fetchProducts(any(Pageable.class));
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

        verify(model).addAttribute("id", 1L);
        verify(model).addAttribute(eq("newProduct"), any(Product.class));
    }

    // =========================
    // DETAIL PAGE
    // =========================

    @Test
    void getDetailPage_ShouldReturnView() {

        when(productService.fetchProductById(1L))
                .thenReturn(Optional.of(product));

        String view = productController.getProductDetailPage(model, 1L);

        assertEquals("admin/product/detail", view);

        verify(model).addAttribute("product", product);
        verify(model).addAttribute("id", 1L);
    }

    @Test
    void getDetailPage_NotFound_ShouldRedirect() {

        when(productService.fetchProductById(1L))
                .thenReturn(Optional.empty());

        String view = productController.getProductDetailPage(model, 1L);

        assertEquals("redirect:/admin/product", view);

        verify(model, never()).addAttribute(eq("product"), any());
    }

    // =========================
    // UPDATE PAGE
    // =========================

    @Test
    void getUpdatePage_ShouldReturnView() {

        when(productService.fetchProductById(1L))
                .thenReturn(Optional.of(product));

        String view = productController.getUpdateProductPage(model, 1L);

        assertEquals("admin/product/update", view);

        verify(model).addAttribute("newProduct", product);
    }

    @Test
    void getUpdatePage_NotFound_ShouldRedirect() {

        when(productService.fetchProductById(1L))
                .thenReturn(Optional.empty());

        String view = productController.getUpdateProductPage(model, 1L);

        assertEquals("redirect:/admin/product", view);

        verify(model, never()).addAttribute(eq("newProduct"), any());
    }

    // =========================
    // CREATE PRODUCT
    // =========================

    @Test
    void createProduct_ShouldRedirect() {

        when(bindingResult.hasErrors()).thenReturn(false);

        when(uploadService.handleSaveUploadFile(file, "product"))
                .thenReturn("img.png");

        String view = productController.createProduct(
                product,
                bindingResult,
                file);

        assertEquals("redirect:/admin/product", view);

        assertEquals("img.png", product.getImage());

        verify(uploadService).handleSaveUploadFile(file, "product");
        verify(productService).createProduct(product);
    }

    @Test
    void createProduct_HasErrors_ShouldReturnCreateView() {

        when(bindingResult.hasErrors()).thenReturn(true);

        String view = productController.createProduct(
                product,
                bindingResult,
                file);

        assertEquals("admin/product/create", view);

        verify(uploadService, never())
                .handleSaveUploadFile(any(), anyString());

        verify(productService, never())
                .createProduct(any());
    }

    // =========================
    // DELETE PRODUCT
    // =========================

    @Test
    void deleteProduct_ShouldRedirect() {

        String view = productController.postDeleteProduct(model, product);

        assertEquals("redirect:/admin/product", view);

        verify(productService).deleteProductById(1L);
    }

    // =========================
    // UPDATE PRODUCT
    // =========================

    @Test
    void updateProduct_ShouldRedirect_WhenNoNewImage() {

        Product current = new Product();
        current.setId(1L);

        when(bindingResult.hasErrors()).thenReturn(false);

        when(productService.fetchProductById(1L))
                .thenReturn(Optional.of(current));

        when(file.isEmpty()).thenReturn(true);

        String view = productController.handleUpdateProduct(
                product,
                bindingResult,
                file);

        assertEquals("redirect:/admin/product", view);

        verify(productService).createProduct(current);

        verify(uploadService, never())
                .handleSaveUploadFile(any(), anyString());
    }

    @Test
    void updateProduct_ShouldRedirect_WhenUploadNewImage() {

        Product current = new Product();
        current.setId(1L);

        when(bindingResult.hasErrors()).thenReturn(false);

        when(productService.fetchProductById(1L))
                .thenReturn(Optional.of(current));

        when(file.isEmpty()).thenReturn(false);

        when(uploadService.handleSaveUploadFile(file, "product"))
                .thenReturn("newImage.png");

        String view = productController.handleUpdateProduct(
                product,
                bindingResult,
                file);

        assertEquals("redirect:/admin/product", view);

        assertEquals("newImage.png", current.getImage());

        verify(uploadService).handleSaveUploadFile(file, "product");
        verify(productService).createProduct(current);
    }

    @Test
    void updateProduct_HasErrors_ShouldReturnUpdateView() {

        when(bindingResult.hasErrors()).thenReturn(true);

        String view = productController.handleUpdateProduct(
                product,
                bindingResult,
                file);

        assertEquals("admin/product/update", view);

        verify(productService, never()).fetchProductById(anyLong());
    }
}