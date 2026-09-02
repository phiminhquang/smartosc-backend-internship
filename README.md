\# SmartOSC Backend Internship



Repository tổng hợp các bài tập và project em thực hiện trong quá trình

thực tập vị trí \*\*Backend Intern tại SmartOSC\*\*.



Nội dung thực hành được triển khai theo lộ trình từ kiến thức Java nền tảng,

lập trình hướng đối tượng, Collection, xử lý dữ liệu, cơ sở dữ liệu đến

phát triển Backend với Spring Boot.



\---



\## 1. Java Core



Các bài tập trong giai đoạn đầu tập trung vào việc củng cố kiến thức nền tảng

của Java, bao gồm:



\- Biến, kiểu dữ liệu và toán tử

\- Cấu trúc điều kiện và vòng lặp

\- Mảng và String

\- Class và Object

\- Encapsulation

\- Inheritance

\- Polymorphism

\- Abstract Class và Interface

\- Exception Handling

\- String Pool

\- Lambda Expression

\- Stream API

\- Multithreading

\- Thread và Runnable



Các bài tập nhỏ được lưu trong các project thực hành theo từng chủ đề và

từng giai đoạn học tập.



\---



\## 2. Collection và xử lý dữ liệu



Thực hành sử dụng các cấu trúc dữ liệu và API phổ biến trong Java:



\- List

\- Set

\- Map

\- Collection Framework

\- Lambda Expression

\- Stream API

\- Xử lý và biến đổi dữ liệu

\- JSON

\- File I/O



Thông qua các bài tập này, em làm quen với cách lựa chọn cấu trúc dữ liệu

phù hợp và xử lý dữ liệu bằng các API có sẵn của Java.



\---



\## 3. Maven, Database và Docker



Trong giai đoạn tiếp theo, các nội dung được thực hành gồm:



\- Maven và quản lý dependency

\- Cấu trúc project Maven

\- MySQL

\- SQL

\- Kết nối và thao tác với cơ sở dữ liệu

\- Docker cơ bản



Đây là bước chuẩn bị trước khi chuyển sang xây dựng các ứng dụng Backend

với Spring Boot.



\---



\## 4. Student Management



`Student Management` là project Spring Boot được sử dụng để thực hành

các thành phần cơ bản của một ứng dụng Backend.



Các nội dung chính:



\- REST API

\- Controller - Service - Repository

\- Spring Data JPA

\- MySQL

\- Entity và quan hệ giữa các Entity

\- DTO

\- Mapper

\- Validation

\- Exception Handling



Project giúp em làm quen với cách tổ chức source code theo nhiều layer và

luồng xử lý request trong ứng dụng Spring Boot.



\---



\## 5. Authentication và Role-Based Access Control



Project thực hành Authentication và phân quyền được xây dựng nhằm tìm hiểu:



\- User

\- Role

\- User - Role Relationship

\- Authentication

\- Authorization

\- Role-Based Access Control (RBAC)

\- Spring Security

\- JWT



Nội dung này được sử dụng làm nền tảng cho việc triển khai Authentication

và Authorization trong project Device Management.



\---



\## 6. Device Management



`Device Management` là \*\*project thực hành trọng tâm trong kỳ thực tập\*\*.



Project ban đầu được xây dựng từ các yêu cầu quản lý thiết bị cơ bản,

sau đó tiếp tục được mở rộng nhằm thực hành thêm các kiến thức Backend

và xử lý business logic với Spring Boot.



\### Các chức năng và nội dung chính



\- Authentication và Role-Based Access Control

\- User và Role Management

\- Device Management

\- Device Lifecycle Management

\- Device Assignment

\- Device Return

\- Assignment Extension

\- Repair Management

\- Scheduler / Cronjob

\- Overdue Assignment Processing

\- Email Notification với JavaMailSender

\- Search

\- Filter

\- Pagination

\- Dashboard

\- Validation

\- Global Exception Handling

\- DTO và Mapper

\- Swagger / OpenAPI

\- Automated Testing



\### Một số công nghệ được sử dụng



\- Spring Boot

\- Spring Data JPA

\- Spring Security

\- MySQL

\- JWT

\- JavaMailSender

\- Spring Scheduler

\- Swagger / OpenAPI

\- JUnit



Thông qua project này, em có cơ hội kết hợp các kiến thức đã học ở những

giai đoạn trước và tiếp tục mở rộng project với nhiều chức năng và

business rule hơn.



\---



\## Technologies



Các công nghệ và công cụ chính được sử dụng trong quá trình thực tập:



\- Java

\- Spring Boot

\- Spring Data JPA

\- Spring Security

\- Maven

\- MySQL

\- Docker

\- JWT

\- JavaMailSender

\- Swagger / OpenAPI

\- JUnit

\- Git

\- GitHub

\- IntelliJ IDEA



\---



\## Repository Structure



Repository bao gồm các bài tập nhỏ và các project được thực hiện trong

từng giai đoạn học tập.



```text

IdeaProjects/

│

├── Java Core \& OOP Practice

├── Collection / Stream / Multithreading Practice

├── JSON \& Data Processing Practice

├── Maven \& Database Practice

│

├── student-management

│

├── ex-rbac-complete

│

└── device

```



Một số thư mục bài tập được giữ theo tên project ban đầu trong quá trình

thực hành.



\---



\## Security



Các thông tin nhạy cảm không được lưu trực tiếp trong source code, bao gồm:



\- Database password

\- Email application password

\- JWT signing key

\- Các thông tin xác thực khác



Những giá trị này được cung cấp cho ứng dụng thông qua

\*\*Environment Variables\*\*.



Ví dụ:



```yaml

spring:

&#x20; datasource:

&#x20;   username: ${DB\_USERNAME}

&#x20;   password: ${DB\_PASSWORD}



&#x20; mail:

&#x20;   username: ${MAIL\_USERNAME}

&#x20;   password: ${MAIL\_PASSWORD}



jwt:

&#x20; signer-key: ${JWT\_SIGNER\_KEY}

```



\---



\## Internship Information



\- \*\*Company:\*\* SmartOSC

\- \*\*Position:\*\* Backend Intern

\- \*\*Main stack:\*\* Java / Spring Boot

\- \*\*Main practice project:\*\* Device Management



\---



\## Purpose



Repository được sử dụng để lưu trữ và tổng hợp mã nguồn các bài tập,

project thực hành trong quá trình thực tập, đồng thời làm tài liệu tham khảo

cho báo cáo tổng kết thực tập.

