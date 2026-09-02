package com.example.test.repository;

import com.example.test.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository
        extends JpaRepository<Course, Long> {

    @Query(
            value = """
                SELECT *
                FROM courses
                """,
            nativeQuery = true
    )
    List<Course> getAllCourses();

    @Query(
            value = """
                SELECT *
                FROM courses
                WHERE id = :id
                """,
            nativeQuery = true
    )
    Course findCourseById(
            @Param("id") Long id
    );

    @Modifying
    @Query(
            value = """
                INSERT INTO courses (name, description, duration)
                SELECT :name, :description, :duration
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM courses
                    WHERE name = :name
                )
                """,
            nativeQuery = true
    )
    int addCourse(
            @Param("name") String name,
            @Param("description") String description,
            @Param("duration") Integer duration
    );

    @Modifying
    @Query(
            value = """
                UPDATE courses AS c
                LEFT JOIN courses AS other
                    ON other.name = :name
                    AND other.id <> :id
                SET c.name = :name,
                    c.description = :description,
                    c.duration = :duration
                WHERE c.id = :id
                  AND other.id IS NULL
                """,
            nativeQuery = true
    )
    int updateCourse(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("description") String description,
            @Param("duration") Integer duration
    );

    @Modifying
    @Query(
            value = """
                DELETE FROM courses
                WHERE id = :id
                """,
            nativeQuery = true
    )
    int deleteCourse(
            @Param("id") Long id
    );
}