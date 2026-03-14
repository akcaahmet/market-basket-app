package com.example.restaurant.model;

public class Register {
    public String account;
    public String fullname;
    public String mobile;
    public String password;
    public String activation_code;

    public Register(String account, String fullname, String mobile, String password, String activation_code) {
        this.account = account;
        this.fullname = fullname;
        this.mobile = mobile;
        this.password = password;
        this.activation_code = activation_code;
    }
}
