package com.example.ex.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
//getter setter
@Data
//tạo cons ko có tham số nmataijij sao có builder r lại cần noarg và allarg(có thể ko cx đc) vì builder nó cx đã làm đc nhieemj vụ đó r mà

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", length = 36, nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
// buider defualt này là để khi ch có gì thì cx sẽ tạo 1 mảng set rỗng ban đầu để sau thêm tránh khi trỏ vào sẽ bị lỗi null
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Roles> roles = new HashSet<>();
}
