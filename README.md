# SmartOSC Backend Internship

Repository tổng hợp các bài tập và project em thực hiện trong quá trình thực tập vị trí **Backend Intern tại SmartOSC**.

Nội dung thực hành được triển khai theo lộ trình từ kiến thức Java nền tảng, lập trình hướng đối tượng, Collection, xử lý dữ liệu, cơ sở dữ liệu đến phát triển Backend với Spring Boot.

---

## 1. Java Core

Các bài tập trong giai đoạn đầu tập trung vào việc củng cố kiến thức nền tảng của Java, bao gồm:

- Biến, kiểu dữ liệu và toán tử
- Cấu trúc điều kiện và vòng lặp
- Mảng và String
- Class và Object
- Encapsulation
- Inheritance
- Polymorphism
- Abstract Class và Interface
- Exception Handling
- String Pool
- Lambda Expression
- Stream API
- Multithreading
- Thread và Runnable

Các bài tập nhỏ được lưu theo từng project và từng chủ đề trong quá trình thực hành.

---

## 2. Collection và xử lý dữ liệu

Các nội dung thực hành gồm:

- List
- Set
- Map
- Collection Framework
- Lambda Expression
- Stream API
- Xử lý và biến đổi dữ liệu
- JSON
- File I/O

Thông qua các bài tập này, em làm quen với việc lựa chọn cấu trúc dữ liệu phù hợp và sử dụng các API của Java để xử lý dữ liệu.

---

## 3. Maven, Database và Docker

Trong giai đoạn tiếp theo, các nội dung được thực hành gồm:

- Maven và quản lý dependency
- Cấu trúc project Maven
- SQL
- MySQL
- Kết nối và thao tác với cơ sở dữ liệu
- Docker cơ bản

Đây là bước chuẩn bị trước khi chuyển sang xây dựng các ứng dụng Backend với Spring Boot.

---

## 4. Student Management

`Student Management` là project Spring Boot được xây dựng nhằm thực hành các thành phần cơ bản của một ứng dụng Backend.

Các nội dung chính:

- REST API
- Controller - Service - Repository
- Spring Data JPA
- MySQL
- Entity và quan hệ giữa các Entity
- DTO
- Mapper
- Validation
- Exception Handling

Project giúp em làm quen với cách tổ chức source code theo nhiều layer và luồng xử lý request trong ứng dụng Spring Boot.

---

## 5. Authentication và Role-Based Access Control

Project thực hành Authentication và phân quyền được xây dựng nhằm tìm hiểu:

- User
- Role
- User - Role Relationship
- Authentication
- Authorization
- Role-Based Access Control (RBAC)
- Spring Security
- JWT

Các kiến thức này được sử dụng làm nền tảng cho việc triển khai Authentication và Authorization trong project Device Management.

---

## 6. Device Management

`Device Management` là **project thực hành trọng tâm trong kỳ thực tập**.

Project ban đầu được xây dựng từ các yêu cầu quản lý thiết bị cơ bản. Sau đó, project tiếp tục được mở rộng nhằm thực hành thêm các kiến thức Backend, Spring Boot và xử lý business logic.

### Các chức năng và nội dung chính

- Authentication và Role-Based Access Control
- User và Role Management
- Device Management
- Device Lifecycle Management
- Device Assignment
- Device Return
- Assignment Extension
- Repair Management
- Scheduler / Cronjob
- Overdue Assignment Processing
- Email Notification với JavaMailSender
- Search
- Filter
- Pagination
- Dashboard
- Validation
- Global Exception Handling
- DTO và Mapper
- Swagger / OpenAPI
- Automated Testing

### Công nghệ sử dụng trong project

- Spring Boot
- Spring Data JPA
- Spring Security
- MySQL
- JWT
- JavaMailSender
- Spring Scheduler
- Swagger / OpenAPI
- JUnit

Thông qua project này, em có cơ hội kết hợp các kiến thức đã học ở những giai đoạn trước và tiếp tục mở rộng project với nhiều chức năng và business rule hơn.

---

## Technologies

Các công nghệ và công cụ chính được sử dụng trong quá trình thực tập:

- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- Maven
- MySQL
- Docker
- JWT
- JavaMailSender
- Swagger / OpenAPI
- JUnit
- Git
- GitHub
- IntelliJ IDEA

---

## Repository Structure

Repository bao gồm các bài tập nhỏ và project được thực hiện trong từng giai đoạn học tập.

```text
IdeaProjects/
│
├── Các bài tập Java Core và OOP
├── Các bài tập Collection, Stream và Multithreading
├── Các bài tập JSON và xử lý dữ liệu
├── Các bài thực hành Maven và Database
│
├── student-management
├── ex-rbac-complete
└── device
```

Một số thư mục bài tập được giữ nguyên tên project ban đầu trong quá trình thực hành.

---

## Security

Các thông tin nhạy cảm không được lưu trực tiếp trong source code, bao gồm:

- Database password
- Email application password
- JWT signing key
- Các thông tin xác thực khác

Các giá trị này được cung cấp cho ứng dụng thông qua **Environment Variables**.

Ví dụ:

```yaml
spring:
  datasource:
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  mail:
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

jwt:
  signer-key: ${JWT_SIGNER_KEY}
```

---

## Internship Information

- **Company:** SmartOSC
- **Position:** Backend Intern
- **Main Stack:** Java / Spring Boot
- **Main Practice Project:** Device Management

---

## Purpose

Repository được sử dụng để lưu trữ và tổng hợp mã nguồn các bài tập và project thực hành trong quá trình thực tập, đồng thời làm tài liệu tham khảo cho báo cáo tổng kết thực tập.
