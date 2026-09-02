package com.example.test.service;

import com.example.test.dto.CourseRequest;
import com.example.test.dto.CourseResponse;
import com.example.test.exception.CourseNotFoundException;
import com.example.test.exception.DuplicateCourseNameException;
import com.example.test.mapper.CourseMapper;
import com.example.test.model.Course;
import com.example.test.repository.CourseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public CourseService(
            CourseRepository courseRepository,
            CourseMapper courseMapper
    ) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.getAllCourses()
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    public CourseResponse findCourseById(Long id) {
        Course course =
                courseRepository.findCourseById(id);

        if (course == null) {
            throw new CourseNotFoundException(
                    "Không tìm thấy khóa học có ID: " + id
            );
        }

        return courseMapper.toResponse(course);
    }

    @Transactional
    public int addCourse(CourseRequest request) {
        int result = courseRepository.addCourse(
                request.getName(),
                request.getDescription(),
                request.getDuration()
        );

        if (result == 0) {
            throw new DuplicateCourseNameException(
                    "Tên khóa học đã tồn tại"
            );
        }

        return result;
    }

    @Transactional
    public int updateCourse(
            Long id,
            CourseRequest request
    ) {
        Course existingCourse =
                courseRepository.findCourseById(id);

        if (existingCourse == null) {
            throw new CourseNotFoundException(
                    "Không tìm thấy khóa học cần cập nhật"
            );
        }

        int result = courseRepository.updateCourse(
                id,
                request.getName(),
                request.getDescription(),
                request.getDuration()
        );

        if (result == 0) {
            throw new DuplicateCourseNameException(
                    "Tên khóa học đã được sử dụng"
            );
        }

        return result;
    }

    @Transactional
    public int deleteCourse(Long id) {
        int result =
                courseRepository.deleteCourse(id);

        if (result == 0) {
            throw new CourseNotFoundException(
                    "Không tìm thấy khóa học cần xóa"
            );
        }

        return result;
    }
}