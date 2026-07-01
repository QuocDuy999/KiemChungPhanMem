package com.example.food_store.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.food_store.config.TestConfig;
import com.example.food_store.domain.Product;
import com.example.food_store.repository.specification.ProductSpecification;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    private Product createProduct(String name, String type, double price) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDetailDesc("Detail");
        product.setShortDesc("Short");
        product.setQuantity(10);
        product.setSource("Viet Nam");
        product.setUnit("Kg");
        product.setType(type);
        product.setTarget("All");
        product.setCustomerTarget("All");
        return product;
    }

    @Test
    void count_ReturnNumberOfProducts() {

        entityManager.persist(createProduct("Apple", "Fruit", 10));
        entityManager.persist(createProduct("Orange", "Fruit", 20));
        entityManager.flush();

        long result = productRepository.count();

        assertEquals(2, result);
    }

    @Test
    void countByType_ReturnCorrectCount() {

        entityManager.persist(createProduct("Apple", "Fruit", 10));
        entityManager.persist(createProduct("Orange", "Fruit", 20));
        entityManager.persist(createProduct("Milk", "Drink", 30));
        entityManager.flush();

        long result = productRepository.countByType("Fruit");

        assertEquals(2, result);
    }

    @Test
    void findByType_ReturnProducts() {

        entityManager.persist(createProduct("Apple", "Fruit", 10));
        entityManager.persist(createProduct("Orange", "Fruit", 20));
        entityManager.persist(createProduct("Milk", "Drink", 30));
        entityManager.flush();

        List<Product> result = productRepository.findByType("Fruit");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getType().equals("Fruit")));
    }

    @Test
    void findByType_ReturnEmpty_WhenTypeNotExists() {

        List<Product> result = productRepository.findByType("Unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_Pageable_ReturnPage() {

        entityManager.persist(createProduct("Apple", "Fruit", 10));
        entityManager.persist(createProduct("Orange", "Fruit", 20));
        entityManager.persist(createProduct("Milk", "Drink", 30));
        entityManager.flush();

        Page<Product> page = productRepository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }

    @Test
    void findAll_WithSpecification_ReturnFilteredProducts() {

        entityManager.persist(createProduct("Apple", "Fruit", 10));
        entityManager.persist(createProduct("Orange", "Fruit", 20));
        entityManager.persist(createProduct("Milk", "Drink", 30));
        entityManager.flush();

        Page<Product> page = productRepository.findAll(
                ProductSpecification.nameLike("Apple"),
                PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Apple", page.getContent().get(0).getName());
    }

}