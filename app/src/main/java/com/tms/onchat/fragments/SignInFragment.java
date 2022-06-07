package com.tms.onchat.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tms.onchat.R;
import com.tms.onchat.databinding.FragmentSignInBinding;
import com.tms.onchat.listeners.SignInOrUpListener;

public class SignInFragment extends Fragment {

    private FragmentSignInBinding binding;

    private SignInOrUpListener signInOrUpListener;

    public SignInFragment(SignInOrUpListener signInOrUpListener) {
        this.signInOrUpListener = signInOrUpListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(getLayoutInflater());

        setListeners();

        return binding.getRoot();
    }

    private void setListeners() {
        binding.btnSignIn.setOnClickListener(v -> {
            if(checkValidDetails()) {
                signInOrUpListener.signInUserByEmailPassword(
                        binding.edtInputEmail.getText().toString().trim(),
                        binding.edtInputPassword.getText().toString().trim()
                );
            }
        });
    }

    /** TODO: Check details inputed is valid to sign in */
    private boolean checkValidDetails() {
        String email = binding.edtInputEmail.getText().toString().trim();
        String password = binding.edtInputPassword.getText().toString().trim();

        if(email.isEmpty() || email.equals("") || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast(getString(R.string.toast_email_invalid));
            return false;
        }

        if(password.isEmpty() || password.equals("") || password.length() < 6) {
            showToast(getString(R.string.toast_password_invalid));
            return false;
        }

        return true;
    }

    private void showToast(String mes) {
        Toast.makeText(getContext(), mes, Toast.LENGTH_SHORT).show();
    }
}