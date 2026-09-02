package com.example.ex.repository;

import com.example.ex.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
//kế thừa các ham crud có sẵn như save byFindId... đấy xây các query ở đây cx đc nếu ko muốn dùng hàm có sẵn
// và sẽ có 2 đầu vào 1 là Entity mà nó quản lý 2 là khóa chhinhs của nó
public interface RoleRepository extends JpaRepository<Roles, Integer> {
    boolean existsByName(String name);
    Optional<Roles> findByName(String name);
}
