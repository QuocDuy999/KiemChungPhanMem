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

    // =====================================================
    // Helper - Product hợp lệ
    // =====================================================
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

    // =====================================================
    // TC01 - Valid Product
    // =====================================================

    @Test
    void TC01_validProduct() {
        Product p = validProduct();
        assertTrue(validator.validate(p).isEmpty());
    }

    // =====================================================
    // NAME
    // =====================================================

    @Test
    void TC02_nameNull() {
        Product p = validProduct();
        p.setName(null);

        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC03_nameEmpty() {
        Product p = validProduct();
        p.setName("");

        assertFalse(validator.validate(p).isEmpty());
    }

    // =====================================================
    // PRICE
    // =====================================================

    @Test
    void TC04_priceMinusOne() {
        Product p = validProduct();
        p.setPrice(-1);

        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC05_priceZero() {
        Product p = validProduct();
        p.setPrice(0);

        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC06_priceMinPlus() {
        Product p = validProduct();
        p.setPrice(0.01);

        assertTrue(validator.validate(p).isEmpty());
    }

    // =====================================================
    // QUANTITY
    // =====================================================

    @Test
    void TC07_quantityZero() {
        Product p = validProduct();
        p.setQuantity(0);

        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC08_quantityMin() {
        Product p = validProduct();
        p.setQuantity(1);

        assertTrue(validator.validate(p).isEmpty());
    }

    @Test
    void TC09_quantityMinPlus() {
        Product p = validProduct();
        p.setQuantity(2);

        assertTrue(validator.validate(p).isEmpty());
    }

    // =====================================================
    // DETAIL DESCRIPTION
    // =====================================================

    @Test
    void TC10_detailDescNull() {
        Product p = validProduct();
        p.setDetailDesc(null);

        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC11_detailDescEmpty() {
        Product p = validProduct();
        p.setDetailDesc("");

        assertFalse(validator.validate(p).isEmpty());
    }

    // =====================================================
    // SHORT DESCRIPTION
    // =====================================================

    @Test
    void TC12_shortDescNull() {
        Product p = validProduct();
        p.setShortDesc(null);

        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC13_shortDescEmpty() {
        Product p = validProduct();
        p.setShortDesc("");

        assertFalse(validator.validate(p).isEmpty());
    }

    // =====================================================
    // SOURCE
    // =====================================================

    @Test
    void TC14_sourceNull() {
        Product p = validProduct();
        p.setSource(null);

        assertFalse(validator.validate(p).isEmpty());
    }

    // =====================================================
    // UNIT
    // =====================================================

    @Test
    void TC15_unitNull() {
        Product p = validProduct();
        p.setUnit(null);

        assertFalse(validator.validate(p).isEmpty());
    }

    // =====================================================
    // BOUNDARY COMBINATION
    // =====================================================

    @Test
    void TC16_priceMinPlus_quantityMin() {
        Product p = validProduct();

        p.setPrice(0.01);
        p.setQuantity(1);

        assertTrue(validator.validate(p).isEmpty());
    }

    @Test
    void TC17_priceInvalid_quantityValid() {
        Product p = validProduct();

        p.setPrice(0);
        p.setQuantity(1);

        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC18_priceValid_quantityInvalid() {
        Product p = validProduct();

        p.setPrice(10);
        p.setQuantity(0);

        assertFalse(validator.validate(p).isEmpty());
    }

    // =====================================================
    // FULL INVALID
    // =====================================================

    @Test
    void TC19_allInvalid() {

        Product p = new Product();

        p.setName("");
        p.setPrice(-1);
        p.setQuantity(0);
        p.setDetailDesc("");
        p.setShortDesc("");
        p.setSource(null);
        p.setUnit(null);

        assertFalse(validator.validate(p).isEmpty());
    }

    // =====================================================
    // ALL BOUNDARY VALID
    // =====================================================

    @Test
    void TC20_allBoundaryValid() {

        Product p = validProduct();

        p.setPrice(0.01);
        p.setQuantity(1);

        assertTrue(validator.validate(p).isEmpty());
    }

}