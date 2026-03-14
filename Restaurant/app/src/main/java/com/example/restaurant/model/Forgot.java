package com.example.restaurant.model;

public class Forgot {
    public String account;
    public String password;
    public String confirm;

    public Forgot(String account, String confirm, String password) {
        this.account = account;
        this.confirm = confirm;
        this.password = password;
    }
}
