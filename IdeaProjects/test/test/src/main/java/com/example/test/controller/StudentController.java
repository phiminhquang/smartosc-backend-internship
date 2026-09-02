package com.example.test.controller;

import com.example.test.dto.StudentRequest;
import com.example.test.dto.StudentResponse;
import com.example.test.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<StudentResponse> getAll() {
        return studentService.getAll();
    }

    @GetMapping("/{id}")
    public StudentResponse findStudentFollowId(
            @PathVariable("id") Long id
    ) {
        return studentService.findStudentFollowId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public int addStudent(
            @Valid @RequestBody StudentRequest request
    ) {
        return studentService.addStudent(request);
    }

    @PutMapping("/{id}")
    public int updateStudent(
            @PathVariable("id") Long id,
            @Valid @RequestBody StudentRequest request
    ) {
        return studentService.updateStudent(id, request);
    }

    @DeleteMapping("/{id}")
    public int deleteStudent(
            @PathVariable("id") Long id
    ) {
        return studentService.deleteStudent(id);
    }
}