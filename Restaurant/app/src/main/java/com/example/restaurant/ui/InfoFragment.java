package com.example.restaurant.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.restaurant.ui.InfoFragmentDirections;
import com.example.restaurant.R;

public class InfoFragment extends Fragment {

    Button loginButton;
    NavDirections action;

    public InfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_info, container, false);
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        loginButton = view.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> goToLogin());
        return view;
    }

    public void goToLogin() {
        action = InfoFragmentDirections.actionInfoFragmentToLoginFragment2();
        Navigation.findNavController(requireView()).navigate(action);
    }
}