package com.example.student.management.repository;

import com.example.student.management.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentRepository
        extends JpaRepository<Student, Long> {
//    @Query("""
//        SELECT s FROM Student s WHERE fullName = :keyword
//    """)
    @Query(value = "select * from students where full_name = :keyword"
            , nativeQuery = true)
    List<Student> findByFullNameContainingIgnoreCase(String keyword);
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );
}
