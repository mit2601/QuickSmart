package com.example.quicksmart;

public class usermodel {
    String name,email,phn_no;

    public usermodel() {
    }


    public usermodel(String name, String email, String phn_no) {
        this.name = name;
        this.email = email;
        this.phn_no = phn_no;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhn_no() {
        return phn_no;
    }

    public void setPhn_no(String phn_no) {
        this.phn_no = phn_no;
    }
}
