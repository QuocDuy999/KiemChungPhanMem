package com.example.food_store.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;
import com.example.food_store.domain.dto.RegisterDTO;
import com.example.food_store.repository.OrderRepository;
import com.example.food_store.repository.RoleRepository;
import com.example.food_store.repository.UserRepository;
import com.example.food_store.service.IUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder; 

    @Override
    public User handleSaveUser(User user) {
        return this.userRepository.save(user);
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable);
    }

    @Override
    public User getUserById(long id) {
        return this.userRepository.findById(id);
    }

    @Override
    public void saveUser(User user) {
        this.userRepository.save(user);
    }

    @Override
    public void deleteUserById(long id) {
        this.userRepository.deleteById(id);
    }

    @Override
    public Role getRoleByName(String name) {
        return this.roleRepository.findByName(name).orElse(null); 
    }

    @Override
    public User registerDTOtoUser(RegisterDTO registerDTO) {
        User user = new User();
        user.setFullName(registerDTO.getFullName());
        user.setEmail(registerDTO.getEmail());
        
        // 1. Mã hóa mật khẩu thô từ giao diện gửi lên
        user.setPassword(this.passwordEncoder.encode(registerDTO.getPassword()));
        
        // 2. Tự động tìm vai trò ROLE_USER gán cho người dùng mới
        Role defaultRole = this.roleRepository.findByName("ROLE_USER").orElse(null);
        user.setRole(defaultRole);
        
        user.setProvider("LOCAL"); 

        return user;
    }

    @Override
    public boolean checkEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    @Override
    public User getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Override
    public long countUser() {
        return this.userRepository.count();
    }

    @Override
    public long countOrder() {
        return this.orderRepository.count();
    }
}