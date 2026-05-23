package com.syfe.finance.model;

public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String phoneNumber;

    public User(Long id, String username, String passwordHash, String fullName, String phoneNumber) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
