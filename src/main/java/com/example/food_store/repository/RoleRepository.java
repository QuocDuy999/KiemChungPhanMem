package com.example.food_store.repository;

import java.util.Optional; // <-- CHỖ SỬA: Thêm dòng import này vào

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.food_store.domain.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);
}