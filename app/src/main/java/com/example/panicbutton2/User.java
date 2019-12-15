package com.example.panicbutton2;

import android.widget.EditText;

public class User {

    public String name, email, phone;

    public User(EditText inputName, EditText inputEmail, EditText inputTelp){

    }

    public User(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}
