package com.demo.entity;

import java.util.ArrayList;
import java.util.List;

public class Course {

    private String id;

    private String schoolName;

    private String name;

    private Teacher teacher;

    private List<Student> students;



    public Course() {
    }

    public Course(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Course(String id, String name, Teacher teacher) {
        this.id = id;
        this.name = name;
        this.teacher = teacher;
    }
    public Course(String id, String name, Teacher teacher, String schoolName) {
        this.id = id;
        this.name = name;
        this.teacher = teacher;
        this.schoolName=schoolName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public Teacher getTeacher() {
        return teacher;
    }
    public String getName() {
        return name;
    }
    public String getSchoolName() {
        return schoolName;
    }

    public void addStudent(Student student) {
        if (students == null) {
            students = new ArrayList<>();
        }

        this.students.add(student);
    }

    public void addStudents(List<Student> students) {
        if (students == null) {
            students = new ArrayList<>();
        }

        this.students.addAll(students);
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
    public void showInfor(){
        for(Student student : students ){
            System.out.println("Ten khoa hoc: "+name+" | ID: "+id);
            System.out.println("Name: "+student.getName()+" | Hometown: "+student.getHomeTown()+" | ID: "+student.getId()+" | Email: "+student.getEmail());
            System.out.println("---");
        }
    }
}
