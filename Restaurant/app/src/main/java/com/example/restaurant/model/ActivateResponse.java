package com.example.restaurant.model;

public class ActivateResponse {
    public String message;
    public String activation_code; // kaldırılacak
    public ActivateResponse(String message, String activation_code) {
        this.message = message;
        this.activation_code = activation_code;
    }
}
