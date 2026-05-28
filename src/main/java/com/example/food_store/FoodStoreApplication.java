package com.example.food_store;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.food_store.domain.Role;
import com.example.food_store.domain.User;
import com.example.food_store.repository.RoleRepository;
import com.example.food_store.repository.UserRepository;

@SpringBootApplication
public class FoodStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodStoreApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(
            RoleRepository roleRepository, 
            UserRepository userRepository, 
            PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Khởi tạo các Role mặc định nếu chưa có (Thêm tiền tố ROLE_ để Spring Security hiểu)
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
            Role userRole = roleRepository.findByName("ROLE_USER").orElse(null);

            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                adminRole.setDescription("Administrator Role");
                adminRole = roleRepository.save(adminRole);
                System.out.println("Imported default role: ROLE_ADMIN");
            }

            if (userRole == null) {
                userRole = new Role();
                userRole.setName("ROLE_USER");
                userRole.setDescription("User Role");
                roleRepository.save(userRole);
                System.out.println("Imported default role: ROLE_USER");
            }

            // 2. Khởi tạo tài khoản Admin mặc định một cách an toàn
            if (userRepository.count() == 0) {
                User adminUser = new User();
                adminUser.setEmail("admin@gmail.com");
                adminUser.setPassword(passwordEncoder.encode("123456"));
                adminUser.setFullName("MrTun");
                
                // Gán trực tiếp đối tượng adminRole vừa tìm hoặc tạo ở trên, không lo bị lỗi ID trống
                adminUser.setRole(adminRole); 
                
                adminUser.setProvider("LOCAL");
                adminUser.setAddress("HCM City");
                adminUser.setAvatar("");
                adminUser.setPhone("0333333333");

                userRepository.save(adminUser);
                System.out.println("Imported default admin user thành công: " + adminUser.getEmail());
            }
        };
    }
}