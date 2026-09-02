package com.example.test.controller;

import com.example.test.dto.CourseRequest;
import com.example.test.dto.CourseResponse;
import com.example.test.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseResponse> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public CourseResponse findCourseById(
            @PathVariable("id") Long id
    ) {
        return courseService.findCourseById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public int addCourse(
            @Valid @RequestBody CourseRequest request
    ) {
        return courseService.addCourse(request);
    }

    @PutMapping("/{id}")
    public int updateCourse(
            @PathVariable("id") Long id,
            @Valid @RequestBody CourseRequest request
    ) {
        return courseService.updateCourse(id, request);
    }

    @DeleteMapping("/{id}")
    public int deleteCourse(
            @PathVariable("id") Long id
    ) {
        return courseService.deleteCourse(id);
    }
}