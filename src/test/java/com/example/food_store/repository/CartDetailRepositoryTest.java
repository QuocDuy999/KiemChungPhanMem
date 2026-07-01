package com.example.food_store.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import com.example.food_store.config.TestConfig; 

import com.example.food_store.domain.Cart;
import com.example.food_store.domain.CartDetail;
import com.example.food_store.domain.Product;
import com.example.food_store.domain.User;

@DataJpaTest
@Import(TestConfig.class)
class CartDetailRepositoryTest {

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;


    private Cart cart;
    private Product product;


    @BeforeEach
    void setUp() {

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("123456");
        user.setFullName("Test User");

        user = userRepository.save(user);


        cart = new Cart();
        cart.setUser(user);
        cart.setSum(0);

        cart = cartRepository.save(cart);


        product = new Product();
        product.setName("Pizza");
        product.setPrice(100);
        product.setDetailDesc("Pizza ngon");
        product.setShortDesc("Pizza");
        product.setQuantity(10);
        product.setSource("Vietnam");
        product.setUnit("Cái");

        product = productRepository.save(product);


        CartDetail cartDetail = new CartDetail();
        cartDetail.setCart(cart);
        cartDetail.setProduct(product);
        cartDetail.setQuantity(2);
        cartDetail.setPrice(product.getPrice());

        cartDetailRepository.save(cartDetail);
    }


    @Test
    void testExistsByCartAndProduct_ReturnTrue() {

        boolean result =
                cartDetailRepository.existsByCartAndProduct(cart, product);

        assertTrue(result);
    }


    @Test
    void testExistsByCartAndProduct_ReturnFalse() {

        Product otherProduct = new Product();

        otherProduct.setName("Burger");
        otherProduct.setPrice(50);
        otherProduct.setDetailDesc("Burger ngon");
        otherProduct.setShortDesc("Burger");
        otherProduct.setQuantity(5);
        otherProduct.setSource("Vietnam");
        otherProduct.setUnit("Cái");

        otherProduct = productRepository.save(otherProduct);


        boolean result =
                cartDetailRepository.existsByCartAndProduct(cart, otherProduct);


        assertFalse(result);
    }


    @Test
    void testFindByCartAndProduct_Found() {

        CartDetail result =
                cartDetailRepository.findByCartAndProduct(cart, product);


        assertNotNull(result);
        assertEquals(cart.getId(), result.getCart().getId());
        assertEquals(product.getId(), result.getProduct().getId());
        assertEquals(2, result.getQuantity());
    }


    @Test
    void testFindByCartAndProduct_NotFound() {

        Product otherProduct = new Product();

        otherProduct.setName("Burger");
        otherProduct.setPrice(50);
        otherProduct.setDetailDesc("Burger ngon");
        otherProduct.setShortDesc("Burger");
        otherProduct.setQuantity(5);
        otherProduct.setSource("Vietnam");
        otherProduct.setUnit("Cái");

        otherProduct = productRepository.save(otherProduct);


        CartDetail result =
                cartDetailRepository.findByCartAndProduct(cart, otherProduct);


        assertNull(result);
    }


    @Test
    void testFindByCart_Found() {

        CartDetail result =
                cartDetailRepository.findByCart(cart);


        assertNotNull(result);
        assertEquals(cart.getId(), result.getCart().getId());
    }


    @Test
    void testFindByCart_NotFound() {

        User otherUser = new User();

        otherUser.setEmail("other@gmail.com");
        otherUser.setPassword("123456");
        otherUser.setFullName("Other User");

        otherUser = userRepository.save(otherUser);


        Cart otherCart = new Cart();

        otherCart.setUser(otherUser);
        otherCart.setSum(0);

        otherCart = cartRepository.save(otherCart);


        CartDetail result =
                cartDetailRepository.findByCart(otherCart);


        assertNull(result);
    }
}