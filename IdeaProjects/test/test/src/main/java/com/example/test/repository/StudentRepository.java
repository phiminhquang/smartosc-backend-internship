package com.example.test.repository;

import com.example.test.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student,Long>{
    @Query(
            value = """
        SELECT *
        FROM student
        """,
            nativeQuery = true
    )
    List<Student> getAll();
    @Modifying
    @Query(
            value = """
        INSERT INTO student(name,email,age)
                select :name,:email,:age
                where not exists(
                        select 1
                        from student
                        where email = :email
                        )
        """,
            nativeQuery = true
    )
    int addStudent(
        @Param("name") String name,
        @Param("email") String email,
        @Param("age") Integer age
        );
    @Query(value = """
        SELECT *
        from student
        where id = :id
""",
    nativeQuery = true)
    Student findStudentFollowId(@Param("id")Long id);
    @Modifying
    @Query(value = """
        UPDATE student AS s 
        Left Join student AS other
        On other.email = :email and other.id <> :id 
        set s.name =:name,s.email =:email,s.age =:age
        where id =:id and other.id is null

""",
    nativeQuery = true)
    int updateStudent(@Param("id") Long id,@Param("name") String name,@Param("email" )String email,@Param("age") Integer age);
    @Modifying
    @Query(value = """
        Delete From student
        where id = :id
""",
    nativeQuery = true)
    int deleteStudent(@Param("id")Long id);
}
