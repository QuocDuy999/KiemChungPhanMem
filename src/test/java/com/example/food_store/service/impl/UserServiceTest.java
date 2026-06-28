package com.example.food_store.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;
import com.example.food_store.domain.dto.RegisterDTO;
import com.example.food_store.repository.OrderRepository;
import com.example.food_store.repository.RoleRepository;
import com.example.food_store.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1);
        user.setEmail("cloud@gmail.com");
        user.setFullName("Cloud");
        user.setPassword("123456");

        role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");
    }

    @Test
    void testHandleSaveUser() {

        when(userRepository.save(user))
                .thenReturn(user);

        User result =
                userService.handleSaveUser(user);

        assertEquals(user, result);

        verify(userRepository)
                .save(user);
    }

    @Test
    void testGetAllUsers() {

        PageRequest pageable =
                PageRequest.of(0, 10);

        Page<User> page =
                new PageImpl<>(
                        Collections.singletonList(user));

        when(userRepository.findAll(pageable))
                .thenReturn(page);

        Page<User> result =
                userService.getAllUsers(pageable);

        assertEquals(
                1,
                result.getTotalElements());

        verify(userRepository)
                .findAll(pageable);
    }

    @Test
    void testGetUserById() {

        when(userRepository.findById(1L))
                .thenReturn(user);

        User result =
                userService.getUserById(1L);

        assertNotNull(result);

        assertEquals(
                1,
                result.getId());

        verify(userRepository)
                .findById(1L);
    }

    @Test
    void testSaveUser() {

        userService.saveUser(user);

        verify(userRepository)
                .save(user);
    }

    @Test
    void testDeleteUserById() {

        userService.deleteUserById(1L);

        verify(userRepository)
                .deleteById(1L);
    }

    @Test
    void testGetRoleByName_Found() {

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(role));

        Role result =
                userService.getRoleByName("ROLE_USER");

        assertNotNull(result);

        assertEquals(
                "ROLE_USER",
                result.getName());

        verify(roleRepository)
                .findByName("ROLE_USER");
    }

    @Test
    void testGetRoleByName_NotFound() {

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.empty());

        Role result =
                userService.getRoleByName("ROLE_USER");

        assertNull(result);

        verify(roleRepository)
                .findByName("ROLE_USER");
    }

    @Test
    void testRegisterDTOtoUser_RoleFound() {

        RegisterDTO dto = new RegisterDTO();

        dto.setFullName("Cloud");
        dto.setEmail("cloud@gmail.com");
        dto.setPassword("123456");

        when(passwordEncoder.encode("123456"))
                .thenReturn("encodedPassword");

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(role));

        User result =
                userService.registerDTOtoUser(dto);

        assertEquals(
                "Cloud",
                result.getFullName());

        assertEquals(
                "cloud@gmail.com",
                result.getEmail());

        assertEquals(
                "encodedPassword",
                result.getPassword());

        assertEquals(
                role,
                result.getRole());

        assertEquals(
                "LOCAL",
                result.getProvider());

        verify(passwordEncoder)
                .encode("123456");

        verify(roleRepository)
                .findByName("ROLE_USER");
    }

    @Test
    void testRegisterDTOtoUser_RoleNotFound() {

        RegisterDTO dto = new RegisterDTO();

        dto.setFullName("Cloud");
        dto.setEmail("cloud@gmail.com");
        dto.setPassword("123456");

        when(passwordEncoder.encode("123456"))
                .thenReturn("encodedPassword");

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.empty());

        User result =
                userService.registerDTOtoUser(dto);

        assertNotNull(result);

        assertNull(result.getRole());

        assertEquals(
                "LOCAL",
                result.getProvider());

        verify(passwordEncoder)
                .encode("123456");

        verify(roleRepository)
                .findByName("ROLE_USER");
    }

    @Test
    void testCheckEmailExist_True() {

        when(userRepository.existsByEmail(
                "cloud@gmail.com"))
                .thenReturn(true);

        boolean result =
                userService.checkEmailExist(
                        "cloud@gmail.com");

        assertTrue(result);

        verify(userRepository)
                .existsByEmail("cloud@gmail.com");
    }

    @Test
    void testCheckEmailExist_False() {

        when(userRepository.existsByEmail(
                "abc@gmail.com"))
                .thenReturn(false);

        boolean result =
                userService.checkEmailExist(
                        "abc@gmail.com");

        assertFalse(result);

        verify(userRepository)
                .existsByEmail("abc@gmail.com");
    }

    @Test
    void testGetUserByEmail() {

        when(userRepository.findByEmail(
                "cloud@gmail.com"))
                .thenReturn(user);

        User result =
                userService.getUserByEmail(
                        "cloud@gmail.com");

        assertNotNull(result);

        assertEquals(
                "cloud@gmail.com",
                result.getEmail());

        verify(userRepository)
                .findByEmail("cloud@gmail.com");
    }

    @Test
    void testCountUser() {

        when(userRepository.count())
                .thenReturn(100L);

        long result =
                userService.countUser();

        assertEquals(
                100L,
                result);

        verify(userRepository)
                .count();
    }

    @Test
    void testCountOrder() {

        when(orderRepository.count())
                .thenReturn(50L);

        long result =
                userService.countOrder();

        assertEquals(
                50L,
                result);

        verify(orderRepository)
                .count();
    }
    @Test
    void testGetUserById_NotFound() {

        when(userRepository.findById(99L))
                .thenReturn(null);

        User result =
                userService.getUserById(99L);

        assertNull(result);

        verify(userRepository)
                .findById(99L);
    }
}
