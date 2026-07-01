package com.example.food_store.annotation.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StrnogPasswordValidatorTest {

    private StrnogPasswordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StrnogPasswordValidator();
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPasswordIsStrong() {
        assertTrue(validator.isValid("Abcd1234!", null));
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPasswordTooShort() {
        assertFalse(validator.isValid("Ab1!", null));
    }

    @Test
    void isValid_ShouldReturnFalse_WhenMissingUppercase() {
        assertFalse(validator.isValid("abcd1234!", null));
    }

    @Test
    void isValid_ShouldReturnFalse_WhenMissingLowercase() {
        assertFalse(validator.isValid("ABCD1234!", null));
    }

    @Test
    void isValid_ShouldReturnFalse_WhenMissingDigit() {
        assertFalse(validator.isValid("Abcdefg!", null));
    }

    @Test
    void isValid_ShouldReturnFalse_WhenMissingSpecialCharacter() {
        assertFalse(validator.isValid("Abcd1234", null));
    }
}