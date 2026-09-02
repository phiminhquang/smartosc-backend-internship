package com.demo;

import com.demo.entity.Course;
import com.demo.entity.Student;
import com.demo.entity.Teacher;
import com.demo.entity.User;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Teacher teacher = new Teacher("21","NSB");
        teacher.setName("PMQ");
        teacher.setId("teacher1");
        Student student1 = new Student("10","Quan");
        student1.setId("11");
        student1.setName("Quang");
        student1.setHomeTown("Ha Noi");
        student1.setEmail("phiminhquang2@smartosc.com");
        Student student2 = new Student("12","Quang");
        student2.setHomeTown("Ha Tay");
        student2.setEmail("phiminhquang3@gmail.com");
        Course math = new Course("MLP4005", "Math", teacher,"VNU");
        math.addStudent(student1);
        math.addStudent(student2);
        System.out.println(math.getTeacher().getId()+math.getTeacher().getName());
        math.showInfor();

        List<Course> courses = new ArrayList<>();
        courses.add(math);



    }
}
