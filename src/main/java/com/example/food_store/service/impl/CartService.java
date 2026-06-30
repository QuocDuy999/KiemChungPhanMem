package com.example.food_store.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.food_store.domain.Cart;
import com.example.food_store.domain.User;
import com.example.food_store.repository.CartRepository;
import com.example.food_store.service.ICartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    private final CartRepository cartRepository;

    @Override
    public Cart getCartByID(Long id) {
        Optional<Cart> cart = this.cartRepository.findById(id);
        if (cart.isPresent())
            return cart.get();
        return null;
    }
    
    @Override
    public Cart findByUser(User user){
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return cartRepository.findByUser(user);
    }

    @Override
    public Cart saveCart(Cart cart){
        if (cart == null) {
            throw new IllegalArgumentException("Cart cannot be null");
        }
        if (cart.getSum() < 0) {
            throw new IllegalArgumentException("Cart sum cannot be negative");
        }
        return this.cartRepository.save(cart);
    }
}
