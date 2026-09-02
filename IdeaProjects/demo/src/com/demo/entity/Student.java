package com.demo.entity;

public class Student extends User{
    public Student (String id, String name){
        super(id,name);
    }
    @Override
    public String getTitle() {
        return "Student";
    }
    public void show(){
        System.out.println(getHomeTown());
    }
}
