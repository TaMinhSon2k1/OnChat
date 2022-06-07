package com.tms.onchat.listeners;

public interface SignInOrUpListener {
    void signUpUserByEmailPassword(String email, String password);
    void signInUserByEmailPassword(String email, String password);
}
