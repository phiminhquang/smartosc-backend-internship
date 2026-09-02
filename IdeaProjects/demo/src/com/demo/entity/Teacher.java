package com.demo.entity;

public class Teacher extends User{
    public Teacher (String id, String name){
        super(id,name);
    }
    @Override
    public String getTitle() {
        return "Teacher";
    }
}
