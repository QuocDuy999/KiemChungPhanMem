package com.example.food_store.repository.specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import com.example.food_store.domain.Product;
import com.example.food_store.domain.Product_;

class ProductSpecificationTest {

    private Root<Product> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder builder;

    @SuppressWarnings("rawtypes")
    private Path path;

    private Predicate predicate;

    @BeforeEach
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        builder = mock(CriteriaBuilder.class);

        path = mock(Path.class);
        predicate = mock(Predicate.class);
    }

    @Test
    void testNameLike() {
        when(root.get(Product_.NAME)).thenReturn(path);
        when(builder.like(any(Expression.class), eq("%iphone%"))).thenReturn(predicate);

        Specification<Product> spec = ProductSpecification.nameLike("iphone");

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);
        verify(builder).like(any(Expression.class), eq("%iphone%"));
    }

    @Test
    void testMinPrice() {
        when(root.get(Product_.PRICE)).thenReturn(path);
        when(builder.ge(any(Expression.class), eq(100))).thenReturn(predicate);

        Specification<Product> spec = ProductSpecification.minPrice(100);

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);
        verify(builder).ge(any(Expression.class), eq(100));
    }

    @Test
    void testMaxPrice() {
        when(root.get(Product_.PRICE)).thenReturn(path);
        when(builder.le(any(Expression.class), eq(500))).thenReturn(predicate);

        Specification<Product> spec = ProductSpecification.maxPrice(500);

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);
        verify(builder).le(any(Expression.class), eq(500));
    }

    @Test
    void testMatchPrice() {
        Predicate p1 = mock(Predicate.class);
        Predicate p2 = mock(Predicate.class);

        when(root.get(Product_.PRICE)).thenReturn(path);
        when(builder.gt(any(Expression.class), eq(100))).thenReturn(p1);
        when(builder.le(any(Expression.class), eq(500))).thenReturn(p2);
        when(builder.and(p1, p2)).thenReturn(predicate);

        Specification<Product> spec = ProductSpecification.matchPrice(100, 500);

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);

        verify(builder).gt(any(Expression.class), eq(100));
        verify(builder).le(any(Expression.class), eq(500));
        verify(builder).and(p1, p2);
    }

    @Test
    void testMatchMultiplePrice() {
        when(root.get(Product_.PRICE)).thenReturn(path);
        when(builder.between(any(Expression.class), eq(100), eq(500)))
                .thenReturn(predicate);

        Specification<Product> spec = ProductSpecification.matchMultiplePrice(100, 500);

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);
        verify(builder).between(any(Expression.class), eq(100), eq(500));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testMatchListCustomerTarget() {
        List<String> list = Arrays.asList("Male", "Female");

        CriteriaBuilder.In<Object> in = mock(CriteriaBuilder.In.class);

        when(root.get(Product_.CUSTOMER_TARGET)).thenReturn(path);
        when(builder.in(path)).thenReturn(in);
        when(in.value(list)).thenReturn(in);

        Specification<Product> spec =
                ProductSpecification.matchListcustomerTarget(list);

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);
        verify(in).value(list);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testMatchListType() {
        List<String> list = Arrays.asList("Laptop", "Phone");

        CriteriaBuilder.In<Object> in = mock(CriteriaBuilder.In.class);

        when(root.get(Product_.TYPE)).thenReturn(path);
        when(builder.in(path)).thenReturn(in);
        when(in.value(list)).thenReturn(in);

        Specification<Product> spec = ProductSpecification.matchListType(list);

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);
        verify(in).value(list);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testMatchListTarget() {
        List<String> list = Arrays.asList("Gaming", "Office");

        CriteriaBuilder.In<Object> in = mock(CriteriaBuilder.In.class);

        when(root.get(Product_.TARGET)).thenReturn(path);
        when(builder.in(path)).thenReturn(in);
        when(in.value(list)).thenReturn(in);

        Specification<Product> spec = ProductSpecification.matchListTarget(list);

        Predicate result = spec.toPredicate(root, query, builder);

        assertNotNull(result);
        verify(in).value(list);
    }
}