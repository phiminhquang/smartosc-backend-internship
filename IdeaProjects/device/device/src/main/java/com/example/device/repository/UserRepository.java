package com.example.device.repository;

import com.example.device.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countDistinctByRoles_Name(String roleName);

    List<User> findDistinctByRoles_Name(String roleName);
    List<User> findDistinctByRoles_NameIn(List<String> roleNames);
}
