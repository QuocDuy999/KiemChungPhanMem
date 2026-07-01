package com.example.food_store.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.food_store.config.TestConfig;
import com.example.food_store.domain.User;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("123456");
        user.setFullName("Test User");
        user.setAddress("HCM");
        user.setPhone("0123456789");
        user.setAvatar("");
        user.setProvider("LOCAL");
        return user;
    }

    @Test
    void count_ReturnNumberOfUsers() {

        entityManager.persist(createUser("user1@gmail.com"));
        entityManager.persist(createUser("user2@gmail.com"));
        entityManager.flush();

        long result = userRepository.count();

        assertEquals(3, result);
    }

    @Test
    void existsByEmail_ReturnTrue_WhenEmailExists() {

        User user = createUser("exist@gmail.com");
        entityManager.persist(user);
        entityManager.flush();

        boolean result = userRepository.existsByEmail("exist@gmail.com");

        assertTrue(result);
    }

    @Test
    void existsByEmail_ReturnFalse_WhenEmailNotExists() {

        boolean result = userRepository.existsByEmail("notfound@gmail.com");

        assertFalse(result);
    }

    @Test
    void findByEmail_ReturnUser_WhenEmailExists() {

        User user = createUser("find@gmail.com");
        entityManager.persist(user);
        entityManager.flush();

        User result = userRepository.findByEmail("find@gmail.com");

        assertNotNull(result);
        assertEquals("find@gmail.com", result.getEmail());
    }

    @Test
    void findByEmail_ReturnNull_WhenEmailNotExists() {

        User result = userRepository.findByEmail("abc@gmail.com");

        assertNull(result);
    }

    @Test
    void findById_ReturnUser_WhenIdExists() {

        User user = createUser("id@gmail.com");
        entityManager.persist(user);
        entityManager.flush();

        User result = userRepository.findById(user.getId());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void findById_ReturnNull_WhenIdNotExists() {

        User result = userRepository.findById(999L);

        assertNull(result);
    }

    @Test
    void save_SaveUserSuccessfully() {

        User user = createUser("save@gmail.com");

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser);
        assertTrue(savedUser.getId() > 0);
        assertEquals("save@gmail.com", savedUser.getEmail());
    }

    @Test
    void findAll_ReturnAllUsers() {

        entityManager.persist(createUser("user1@gmail.com"));
        entityManager.persist(createUser("user2@gmail.com"));
        entityManager.flush();

        List<User> users = userRepository.findAll();

        assertEquals(3, users.size());
    }

    @Test
    void deleteById_DeleteUserSuccessfully() {

        User user = createUser("delete@gmail.com");
        entityManager.persist(user);
        entityManager.flush();

        userRepository.deleteById(user.getId());

        entityManager.flush();
        entityManager.clear();

        User result = userRepository.findById(user.getId());

        assertNull(result);
    }
}