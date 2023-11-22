package com.android.login_authentication_app;

public class ReadWriteUserDetails {

    public String doB, gender, phone;

    public ReadWriteUserDetails(String doB, String gender, String phone) {
        this.doB = doB;
        this.gender = gender;
        this.phone = phone;
    }

//    if there is no empty constructor we cannot be able to access data from firebase using snapshot.
    public ReadWriteUserDetails() {}
}
