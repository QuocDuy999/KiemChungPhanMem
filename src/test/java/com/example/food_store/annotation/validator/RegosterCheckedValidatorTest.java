package com.example.food_store.annotation.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.food_store.domain.dto.RegisterDTO;
import com.example.food_store.service.impl.UserService;

import jakarta.validation.ConstraintValidatorContext;

@ExtendWith(MockitoExtension.class)
class RegosterCheckedValidatorTest {

    @Mock
    private UserService userService;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    private RegosterCheckedValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RegosterCheckedValidator(userService);
    }

    private void mockConstraintViolation() {
        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(violationBuilder);

        when(violationBuilder.addPropertyNode(anyString()))
                .thenReturn(nodeBuilder);

        when(nodeBuilder.addConstraintViolation())
                .thenReturn(context);
    }

    private RegisterDTO createDTO() {
        RegisterDTO dto = new RegisterDTO();
        dto.setFullName("Nguyen Van A");
        dto.setEmail("user@gmail.com");
        dto.setPassword("Password123!");
        dto.setConfirmPassword("Password123!");
        return dto;
    }

    @Test
    void isValid_ShouldReturnTrue_WhenPasswordMatchesAndEmailNotExists() {

        RegisterDTO dto = createDTO();

        when(userService.checkEmailExist(dto.getEmail()))
                .thenReturn(false);

        assertTrue(validator.isValid(dto, context));

        verify(userService).checkEmailExist(dto.getEmail());
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPasswordMismatch() {

        mockConstraintViolation();

        RegisterDTO dto = createDTO();
        dto.setConfirmPassword("123");

        when(userService.checkEmailExist(dto.getEmail()))
                .thenReturn(false);

        assertFalse(validator.isValid(dto, context));

        verify(context)
                .buildConstraintViolationWithTemplate("Mật khẩu nhập không chính xác");
    }

    @Test
    void isValid_ShouldReturnFalse_WhenEmailAlreadyExists() {

        mockConstraintViolation();

        RegisterDTO dto = createDTO();

        when(userService.checkEmailExist(dto.getEmail()))
                .thenReturn(true);

        assertFalse(validator.isValid(dto, context));

        verify(context)
                .buildConstraintViolationWithTemplate("Email đã tồn tại");
    }

    @Test
    void isValid_ShouldReturnFalse_WhenPasswordMismatchAndEmailExists() {

        mockConstraintViolation();

        RegisterDTO dto = createDTO();
        dto.setConfirmPassword("123");

        when(userService.checkEmailExist(dto.getEmail()))
                .thenReturn(true);

        assertFalse(validator.isValid(dto, context));

        verify(context)
                .buildConstraintViolationWithTemplate("Mật khẩu nhập không chính xác");

        verify(context)
                .buildConstraintViolationWithTemplate("Email đã tồn tại");
    }
}