package com.example.food_store.validation;

import com.example.food_store.domain.Product;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductCreateValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ===================== HELPER =====================
    private Product validProduct() {
        Product p = new Product();
        p.setName("Milk Tea");
        p.setPrice(10);
        p.setQuantity(10);
        p.setDetailDesc("good");
        p.setShortDesc("short");
        p.setSource("shop");
        p.setUnit("cup");
        return p;
    }

    // ===================================================
    // ===================== EP - NAME ===================
    // ===================================================

    @Test
    void TC01_name_valid() {
        Product p = validProduct();
        p.setName("Milk Tea");
        assertTrue(validator.validate(p).isEmpty());
    }

    @Test
    void TC02_name_null() {
        Product p = validProduct();
        p.setName(null);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC03_name_empty() {
        Product p = validProduct();
        p.setName("");
        assertFalse(validator.validate(p).isEmpty());
    }

    // ===================================================
    // ===================== EP - PRICE ==================
    // ===================================================

    @Test
    void TC04_price_valid() {
        Product p = validProduct();
        p.setPrice(10);
        assertTrue(validator.validate(p).isEmpty());
    }

    @Test
    void TC05_price_null_invalid() {
        Product p = validProduct();
        // primitive double không null được → test case logic giữ EP
        p.setPrice(0);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC06_price_negative() {
        Product p = validProduct();
        p.setPrice(-1);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC07_price_zero() {
        Product p = validProduct();
        p.setPrice(0);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC08_price_min_plus() {
        Product p = validProduct();
        p.setPrice(0.01);
        assertTrue(validator.validate(p).isEmpty());
    }
        // ===================================================
    // ===================== EP - QUANTITY ===============
    // ===================================================

    @Test
    void TC09_quantity_valid() {
        Product p = validProduct();
        p.setQuantity(10);
        assertTrue(validator.validate(p).isEmpty());
    }

    @Test
    void TC10_quantity_zero() {
        Product p = validProduct();
        p.setQuantity(0);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC11_quantity_negative() {
        Product p = validProduct();
        p.setQuantity(-1);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC12_quantity_min_plus() {
        Product p = validProduct();
        p.setQuantity(1);
        assertTrue(validator.validate(p).isEmpty());
    }

    // ===================================================
    // ============ EP - DETAIL DESC / SHORT DESC ========
    // ===================================================

    @Test
    void TC13_detail_null() {
        Product p = validProduct();
        p.setDetailDesc(null);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC14_detail_empty() {
        Product p = validProduct();
        p.setDetailDesc("");
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC15_short_null() {
        Product p = validProduct();
        p.setShortDesc(null);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC16_short_empty() {
        Product p = validProduct();
        p.setShortDesc("");
        assertFalse(validator.validate(p).isEmpty());
    }

    // ===================================================
    // ================= FULL INVALID FORM ===============
    // ===================================================

    @Test
    void TC17_all_invalid() {
        Product p = new Product();

        p.setName("");
        p.setPrice(-1);
        p.setQuantity(0);
        p.setDetailDesc("");
        p.setShortDesc("");

        assertFalse(validator.validate(p).isEmpty());
    }

    // ===================================================
    // ================= BVA EDGE COMBINATION ============
    // ===================================================

    @Test
    void TC18_boundary_all_min_plus() {
        Product p = validProduct();

        p.setPrice(0.01);
        p.setQuantity(1);

        assertTrue(validator.validate(p).isEmpty());
    }

    @Test
    void TC19_boundary_price_fail_quantity_ok() {
        Product p = validProduct();

        p.setPrice(0);
        p.setQuantity(1);

        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC20_boundary_price_ok_quantity_fail() {
        Product p = validProduct();

        p.setPrice(10);
        p.setQuantity(0);

        assertFalse(validator.validate(p).isEmpty());
    }
}