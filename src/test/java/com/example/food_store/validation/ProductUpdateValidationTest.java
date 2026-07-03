package com.example.food_store.validation;

import com.example.food_store.domain.Product;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductUpdateValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Product validProduct() {
        Product p = new Product();

        p.setId(1L);
        p.setName("Milk Tea");
        p.setPrice(100);
        p.setQuantity(10);
        p.setDetailDesc("Good product");
        p.setShortDesc("Short product");
        p.setSource("Vietnam");
        p.setUnit("Kg");
        p.setTarget("tang-can");
        p.setCustomerTarget("tat-ca");
        p.setType("rau");

        return p;
    }

    // ==================================================
    // TC01 - VALID CASE (ONLY ONE)
    // ==================================================

    @Test
    void TC01_AllValid() {
        assertTrue(validator.validate(validProduct()).isEmpty());
    }

    // ==================================================
    // NAME (EP)
    // ==================================================

    @Test
    void TC02_NameNull() {
        Product p = validProduct();
        p.setName(null);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC03_NameEmpty() {
        Product p = validProduct();
        p.setName("");
        assertFalse(validator.validate(p).isEmpty());
    }

    // ==================================================
    // PRICE (BVA) min = 0.01
    // ==================================================

    @Test
    void TC04_PriceNegative() {
        Product p = validProduct();
        p.setPrice(-1);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC05_PriceZero() {
        Product p = validProduct();
        p.setPrice(0);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC06_PriceMinPlus() {
        Product p = validProduct();
        p.setPrice(0.01);
        assertTrue(validator.validate(p).isEmpty());
    }

    // ==================================================
    // QUANTITY (BVA) min = 1
    // ==================================================

    @Test
    void TC07_QuantityNegative() {
        Product p = validProduct();
        p.setQuantity(-1);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC08_QuantityZero() {
        Product p = validProduct();
        p.setQuantity(0);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC09_QuantityMin() {
        Product p = validProduct();
        p.setQuantity(1);
        assertTrue(validator.validate(p).isEmpty());
    }

    // ==================================================
    // DETAIL DESC (EP)
    // ==================================================

    @Test
    void TC10_DetailNull() {
        Product p = validProduct();
        p.setDetailDesc(null);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC11_DetailEmpty() {
        Product p = validProduct();
        p.setDetailDesc("");
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC12_DetailValid() {
        Product p = validProduct();
        assertTrue(validator.validate(p).isEmpty());
    }

    // ==================================================
    // SHORT DESC (EP)
    // ==================================================

    @Test
    void TC13_ShortNull() {
        Product p = validProduct();
        p.setShortDesc(null);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC14_ShortEmpty() {
        Product p = validProduct();
        p.setShortDesc("");
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC15_ShortValid() {
        Product p = validProduct();
        assertTrue(validator.validate(p).isEmpty());
    }

    // ==================================================
    // COMBINATION
    // ==================================================

    @Test
    void TC16_InvalidName() {
        Product p = validProduct();
        p.setName("");
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC17_InvalidPrice() {
        Product p = validProduct();
        p.setPrice(0);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC18_InvalidQuantity() {
        Product p = validProduct();
        p.setQuantity(0);
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC19_InvalidDetail() {
        Product p = validProduct();
        p.setDetailDesc("");
        assertFalse(validator.validate(p).isEmpty());
    }

    @Test
    void TC20_InvalidShort() {
        Product p = validProduct();
        p.setShortDesc("");
        assertFalse(validator.validate(p).isEmpty());
    }

    // ==================================================
    // BOUNDARY COMBO
    // ==================================================

    @Test
    void TC21_BoundaryValid() {
        Product p = validProduct();
        p.setPrice(0.01);
        p.setQuantity(1);
        assertTrue(validator.validate(p).isEmpty());
    }

    // ==================================================
    // ALL INVALID
    // ==================================================

    @Test
    void TC22_AllInvalid() {
        Product p = new Product();

        p.setName("");
        p.setPrice(-1);
        p.setQuantity(0);
        p.setDetailDesc("");
        p.setShortDesc("");
        p.setSource("");
        p.setUnit("");

        assertFalse(validator.validate(p).isEmpty());
    }

    // ==================================================
    // EXTRA FIELDS VALID CHECK
    // ==================================================

    @Test
    void TC23_SourceValid() {
        Product p = validProduct();
        assertTrue(validator.validate(p).isEmpty());
    }

    @Test
    void TC24_UnitValid() {
        Product p = validProduct();
        assertTrue(validator.validate(p).isEmpty());
    }

    @Test
    void TC25_TargetValid() {
        Product p = validProduct();
        assertTrue(validator.validate(p).isEmpty());
    }

    @Test
    void TC26_CustomerTargetValid() {
        Product p = validProduct();
        assertTrue(validator.validate(p).isEmpty());
    }

    @Test
    void TC27_NameValidAgainCheck() {
        Product p = validProduct();
        assertTrue(validator.validate(p).isEmpty());
    }
}