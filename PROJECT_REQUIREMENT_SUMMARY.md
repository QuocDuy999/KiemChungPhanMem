# 📘 PROJECT REQUIREMENT SUMMARY

## FoodStore Management System

### Course

Software Verification & Validation

### Team Information

* Project Name: FoodStore Management System
* Team Size: 6 Members
* Development Methodology: Team-based Software Development
* Repository Management: GitHub
* Project Tracking: Jira

---

# 1. Project Overview

FoodStore Management System là hệ thống quản lý cửa hàng thực phẩm được xây dựng nhằm mô phỏng quy trình phát triển phần mềm thực tế trong môn Kiểm Chứng Phần Mềm.

Hệ thống hỗ trợ:

* Quản lý người dùng
* Quản lý sản phẩm
* Quản lý danh mục
* Quản lý giỏ hàng
* Quản lý đơn hàng
* Xác thực và phân quyền người dùng
* Quản lý dữ liệu cửa hàng

Mục tiêu của dự án là áp dụng quy trình kiểm thử phần mềm, quản lý mã nguồn, theo dõi lỗi và triển khai CI/CD trong môi trường thực tế.

---

# 2. System Architecture

## 2.1 Architecture Model

Hệ thống được xây dựng theo mô hình Client – Server.

Frontend gửi yêu cầu đến Backend thông qua giao thức HTTP.

Backend xử lý:

* Business Logic
* Authentication
* Database Access
* API Response

Dữ liệu được lưu trữ trong MySQL Database.

---

## 2.2 Backend Architecture

Hệ thống sử dụng Spring Boot với kiến trúc nhiều lớp:

### Controller Layer

Xử lý HTTP Request/Response.

### Service Layer

Chứa nghiệp vụ hệ thống.

### Repository Layer

Truy xuất dữ liệu thông qua Spring Data JPA.

### Database Layer

Lưu trữ dữ liệu bằng MySQL.

---

# 3. Technologies Used

## Backend

* Java 17
* Spring Boot
* Spring MVC
* Spring Data JPA
* Spring Security
* Maven

## Database

* MySQL

## Testing

* JUnit 5
* Mockito
* Spring Boot Test

## API Testing

* Postman

## Source Control

* GitHub

## Project Management

* Jira

## CI/CD & Quality Assurance

* Jenkins
* SonarQube
* Docker

---

# 4. Team Responsibilities

Dự án được thực hiện bởi 6 thành viên.

Mỗi thành viên chịu trách nhiệm:

* Phát triển chức năng được phân công
* Viết Unit Test
* Báo cáo lỗi thông qua Jira
* Review Code
* Hỗ trợ kiểm thử hệ thống

---

# 5. Development Workflow

## Step 1 – Requirement Analysis

* Nhận yêu cầu hệ thống
* Phân tích chức năng
* Tạo Task trên Jira

## Step 2 – Development

* Tạo Feature Branch
* Phát triển chức năng
* Commit Source Code

## Step 3 – Testing

* Thực hiện Unit Testing
* Kiểm thử API bằng Postman

## Step 4 – Bug Tracking

Nếu phát hiện lỗi:

* Tạo Jira Issue
* Đính kèm log lỗi
* Assign cho thành viên phụ trách

## Step 5 – Review & Merge

* Tạo Pull Request
* Code Review
* Merge vào Main Branch

---

# 6. Git Workflow

## Branch Structure

main

feature/*

bugfix/*

hotfix/*

## Development Cycle

1. Create Feature Branch
2. Develop Feature
3. Commit Source Code
4. Push to GitHub
5. Create Pull Request
6. Review Code
7. Merge to Main Branch

---

# 7. Jira Workflow

## Task Management

Mỗi chức năng được quản lý bằng Jira Ticket.

Ví dụ:

FOOD-01 User Management

FOOD-02 Product Management

FOOD-03 Order Management

## Bug Management

Khi phát hiện lỗi:

* Tạo Bug Ticket
* Đính kèm hình ảnh/log lỗi
* Assign cho Developer phụ trách
* Verify sau khi fix

---

# 8. Testing Strategy

## Unit Testing

Mục tiêu:

* Kiểm tra Service Layer
* Kiểm tra Business Logic
* Kiểm tra Validation

Công cụ:

* JUnit 5
* Mockito

## API Testing

Kiểm thử:

* Authentication API
* Product API
* Cart API
* Order API

Công cụ:

* Postman

---

# 9. Continuous Integration

## Jenkins Pipeline

Pipeline bao gồm:

1. Source Code Checkout
2. Maven Build
3. Run Unit Tests
4. SonarQube Analysis
5. Build Verification

Build sẽ được kích hoạt tự động khi source code được cập nhật từ GitHub.

---

# 10. Code Quality Control

## SonarQube

Dùng để phân tích:

* Bugs
* Vulnerabilities
* Code Smells
* Maintainability

Tất cả source code phải đạt Quality Gate trước khi được đưa vào Main Branch.

---

# 11. Docker Usage

Docker được sử dụng để triển khai môi trường phát triển và kiểm thử.

Các dịch vụ triển khai bằng Docker:

* Jenkins Server
* SonarQube Server

Lợi ích:

* Chuẩn hóa môi trường
* Dễ dàng triển khai
* Giảm lỗi cấu hình

---

# 12. Security Requirements

Thông tin nhạy cảm không được commit trực tiếp lên GitHub.

Ví dụ:

* Database Password
* Secret Key
* API Key

Sử dụng:

.env

và

.gitignore

để bảo vệ dữ liệu.

---

# 13. Reporting

## Weekly Report

Mỗi tuần nhóm thực hiện:

* Báo cáo tiến độ
* Jira Tickets đã hoàn thành
* Git Commits
* Kết quả Unit Test
* Kết quả SonarQube

## Final Report

Bao gồm:

* System Overview
* Architecture
* Testing Activities
* Jira Workflow
* Git Workflow
* Jenkins Pipeline
* SonarQube Analysis
* Lessons Learned

---

# 14. Project Structure

FoodStore/

├── src/main/java

├── src/main/resources

├── src/test/java

├── Jenkinsfile

├── pom.xml

├── docker-compose.yml

├── reports/

├── logs/

├── .env.example

└── .gitignore

---

# 15. Success Criteria

Dự án được xem là hoàn thành khi:

* Tất cả chức năng hoạt động chính xác
* Unit Test đạt yêu cầu
* API Test thành công
* Jenkins Build thành công
* SonarQube Quality Gate đạt PASS
* Jira và GitHub đồng bộ đầy đủ
* Source Code được quản lý theo Git Workflow

---

# Conclusion

FoodStore Management System được xây dựng theo quy trình phát triển phần mềm chuyên nghiệp với sự hỗ trợ của GitHub, Jira, Jenkins, SonarQube và Docker. Dự án đảm bảo yêu cầu về quản lý mã nguồn, kiểm thử, theo dõi lỗi và kiểm soát chất lượng phần mềm trong môn Kiểm Chứng Phần Mềm.
