package com.example.food_store.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Validation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.food_store.domain.dto.RegisterDTO;
import com.example.food_store.service.impl.UserService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@SpringBootTest
class RegisterDTOValidationTest {
    @Autowired
    private Validator validator;

    @MockBean
    UserService userService;

    // =====================================================
    // TC01
    // =====================================================
    @Test
    void testREG_TC_001_ValidNominalData() {

        RegisterDTO dto = new RegisterDTO();
        dto.setFullName("Trần Thanh Hiển");
        dto.setEmail("hien@gmail.com");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // =====================================================
    // TC02
    // =====================================================
    @Test
    void testREG_TC_002_MinBoundary() {

        RegisterDTO dto = new RegisterDTO();
        dto.setFullName("Nam");
        dto.setEmail("nam@gmail.com");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // =====================================================
    // TC03
    // =====================================================
    @Test
    void testREG_TC_003_MaxBoundary() {

        RegisterDTO dto = new RegisterDTO();

        dto.setFullName("A".repeat(255));
        dto.setEmail("abc@gmail.com");
        dto.setPassword("B".repeat(255));
        dto.setConfirmPassword("B".repeat(255));

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // =====================================================
    // TC04
    // =====================================================
    @Test
    void testREG_TC_004_FullNameTooShort() {

        RegisterDTO dto = new RegisterDTO();

        dto.setFullName("An");
        dto.setEmail("an@gmail.com");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());

        assertTrue(

                violations.stream()

                        .anyMatch(v ->

                        v.getMessage()

                                .contains("Fullname"))

        );

    }

    // =====================================================
    // TC05
    // =====================================================
    @Test
    void testREG_TC_005_PasswordTooShort() {

        RegisterDTO dto = new RegisterDTO();

        dto.setFullName("Nguyễn Văn A");
        dto.setEmail("a@gmail.com");
        dto.setPassword("12345");
        dto.setConfirmPassword("12345");

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());

        assertTrue(

                violations.stream()

                        .anyMatch(v ->

                        v.getMessage()

                                .contains("Mật khẩu"))

        );

    }

    // =====================================================
    // TC07
    // =====================================================
    @Test
    void testREG_TC_007_InvalidEmail() {

        RegisterDTO dto = new RegisterDTO();

        dto.setFullName("Nguyễn Văn A");
        dto.setEmail("abcgmail.com");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());

        assertTrue(

                violations.stream()

                        .anyMatch(v ->

                        v.getMessage()

                                .contains("Email"))

        );

    }

    // =====================================================
    // TC09
    // =====================================================
    @Test
    void testREG_TC_009_EmptyField() {

        RegisterDTO dto = new RegisterDTO();

        dto.setFullName("");
        dto.setEmail("");
        dto.setPassword("");
        dto.setConfirmPassword("");

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());

        assertTrue(violations.size() >= 3);

    }

    // =====================================================
    // TC10
    // =====================================================
    @Test
    void testREG_TC_010_WhiteSpaceName() {

        RegisterDTO dto = new RegisterDTO();

        dto.setFullName("   ");
        dto.setEmail("abc@gmail.com");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");

        dto.setFullName(dto.getFullName().trim());

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());

    }

    // =====================================================
    // TC11
    // =====================================================
    @Test
    void testREG_TC_011_ExceedBoundary() {

        RegisterDTO dto = new RegisterDTO();

        dto.setFullName("A".repeat(256));
        dto.setEmail("abc@gmail.com");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");

        assertTrue(dto.getFullName().length() > 255);

    }

}