package com.example.test.mapper;

import com.example.test.dto.CourseResponse;
import com.example.test.model.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getDuration()
        );
    }
}