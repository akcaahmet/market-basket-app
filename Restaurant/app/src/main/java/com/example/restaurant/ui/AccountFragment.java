package com.example.restaurant.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.restaurant.ui.AccountFragmentDirections;
import com.example.restaurant.R;

public class AccountFragment extends Fragment {
    public AccountFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button button = view.findViewById(R.id.customerPage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin(v);
            }
        });
    }

    public void goToLogin(View view) {
        NavDirections action = AccountFragmentDirections.actionAccountFragmentToLoginFragment();
        Navigation.findNavController(view).navigate(action);
    }

    public void goToLoginRestaurant(View view) {
        NavDirections action = AccountFragmentDirections.actionAccountFragmentToLoginFragment();
        Navigation.findNavController(view).navigate(action);
    }
}