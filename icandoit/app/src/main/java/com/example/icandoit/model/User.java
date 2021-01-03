package com.example.icandoit.model;

public class User {
    private static User user = new User();
    private String id;

    private User(){

    }

    public static User getInstance(){
        return user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
