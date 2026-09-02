package com.example.test.service;

import com.example.test.dto.StudentRequest;
import com.example.test.dto.StudentResponse;
import com.example.test.exception.DuplicateEmailException;
import com.example.test.exception.StudentNotFoundException;
import com.example.test.mapper.StudentMapper;
import com.example.test.model.Student;
import com.example.test.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public StudentService(
            StudentRepository studentRepository,
            StudentMapper studentMapper
    ) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    public List<StudentResponse> getAll() {
        return studentRepository.getAll()
                .stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    public StudentResponse findStudentFollowId(Long id) {
        Student student =
                studentRepository.findStudentFollowId(id);

        if (student == null) {
            throw new StudentNotFoundException(
                    "Không tìm thấy sinh viên có ID: " + id
            );
        }

        return studentMapper.toResponse(student);
    }

    @Transactional
    public int addStudent(StudentRequest request) {
        int result = studentRepository.addStudent(
                request.getName(),
                request.getEmail(),
                request.getAge()
        );

        if (result == 0) {
            throw new DuplicateEmailException(
                    "Email đã tồn tại"
            );
        }

        return result;
    }

    @Transactional
    public int updateStudent(
            Long id,
            StudentRequest request
    ) {
        Student existingStudent =
                studentRepository.findStudentFollowId(id);

        if (existingStudent == null) {
            throw new StudentNotFoundException(
                    "Không tìm thấy sinh viên cần cập nhật"
            );
        }

        int result = studentRepository.updateStudent(
                id,
                request.getName(),
                request.getEmail(),
                request.getAge()
        );

        if (result == 0) {
            throw new DuplicateEmailException(
                    "Email đã được sinh viên khác sử dụng"
            );
        }

        return result;
    }

    @Transactional
    public int deleteStudent(Long id) {
        int result =
                studentRepository.deleteStudent(id);

        if (result == 0) {
            throw new StudentNotFoundException(
                    "Không tìm thấy sinh viên cần xóa"
            );
        }

        return result;
    }
}