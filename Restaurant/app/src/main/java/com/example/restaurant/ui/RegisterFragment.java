package com.example.restaurant.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.restaurant.R;
import com.example.restaurant.model.Forgot;
import com.example.restaurant.ui.RegisterFragmentDirections;
import com.example.restaurant.model.Register;
import com.example.restaurant.network.AccountAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterFragment extends Fragment {
    EditText accountEditText, fullnameEditText, mobileEditText, passwordEditText;
    TextView registerButton;
    AccountAPI accountAPI;
    Retrofit retrofit;
    CompositeDisposable compositeDisposable;
    NavDirections action;
    TextView loginButton;
    TextWatcher textWatcher;
    private String BASE_URL = "https://ahmet16.pythonanywhere.com/";

    public RegisterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        compositeDisposable = new CompositeDisposable();
        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        accountAPI = retrofit.create(AccountAPI.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_register, container, false);
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        accountEditText = view.findViewById(R.id.account);
        fullnameEditText = view.findViewById(R.id.name);
        mobileEditText = view.findViewById(R.id.mobile);
        passwordEditText = view.findViewById(R.id.password);
        registerButton = view.findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> registerX());
        loginButton = view.findViewById(R.id.loginButton);
        textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String account = accountEditText.getText().toString().trim();
                String fullname = fullnameEditText.getText().toString().trim();
                String mobile = mobileEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                boolean accountValid = true;
                boolean fullnameValid = true;
                boolean mobileValid = true;
                boolean passwordValid = true;

                if (account.isEmpty()) {
                    accountEditText.setError("Kullanıcı adı boş olamaz");
                    accountValid = false;
                } else if (account.length() < 5) {
                    accountEditText.setError("Kullanıcı adı en az 5 karakter olmalı");
                    accountValid = false;
                } else {
                    accountEditText.setError(null);
                }
                if (fullname.isEmpty()) {
                    fullnameEditText.setError("Ad soyad boş olamaz");
                    fullnameValid = false;
                } else if (fullname.length() < 3) {
                    fullnameEditText.setError("Ad soyad çok kısa");
                    fullnameValid = false;
                } else {
                    fullnameEditText.setError(null);
                }
                if (mobile.isEmpty()) {
                    mobileEditText.setError("Telefon numarası boş olamaz");
                    mobileValid = false;
                } else if (!mobile.matches("\\d{10,11}")) {
                    mobileEditText.setError("Telefon 10-11 haneli olmalı");
                    mobileValid = false;
                } else {
                    mobileEditText.setError(null);
                }
                if (password.isEmpty()) {
                    passwordEditText.setError("Şifre boş olamaz");
                    passwordValid = false;
                } else if (password.length() < 7) {
                    passwordEditText.setError("Şifre en az 7 karakter olmalı");
                    passwordValid = false;
                } else {
                    passwordEditText.setError(null);
                }
                registerButton.setEnabled(accountValid && fullnameValid && mobileValid && passwordValid);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        accountEditText.addTextChangedListener(textWatcher);
        fullnameEditText.addTextChangedListener(textWatcher);
        mobileEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        registerButton.setEnabled(false);
        loginButton.setOnClickListener(v -> goToLogin());
        return view;
    }

    public void register() {
//        String account = accountEditText.getText().toString();
//        String fullname = fullnameEditText.getText().toString();
//        String mobile = mobileEditText.getText().toString();
//        String password = passwordEditText.getText().toString();
//        System.out.println(account + " " + fullname + " " + mobile + " " + password);
//        Register request = new Register(account, fullname, mobile, password);
//        Forgot temporary = new Forgot(account, password, password);
//        compositeDisposable.add(
//                accountAPI.activateApp(temporary)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(response -> {
//                            String db_code = response.activation_code;
//                            Toast.makeText(getContext(), "Aktivasyon kodu gönderildi", Toast.LENGTH_SHORT).show();
//
//                            LayoutInflater inflater = LayoutInflater.from(getContext());
//                            View dialogView = inflater.inflate(R.layout.dialog, null);
//                            EditText codeEditText = dialogView.findViewById(R.id.codeEditText);
//                            new AlertDialog.Builder(getContext())
//                                    .setTitle("Doğrulama Kodu")
//                                    .setView(dialogView)
//                                    .setPositiveButton("Onayla", (dialog, which) -> {
//                                        String code = codeEditText.getText().toString();
//                                        if (code.length() != 6){
//                                            Toast.makeText(getContext(), "Kod 6 haneli olmalı", Toast.LENGTH_SHORT).show();
//                                            return;
//                                        }
//                                        System.out.println(db_code + " " + code);
//                                        if (db_code.equals(code)) {
//                                            compositeDisposable.add(
//                                                    accountAPI.registerApp(request)
//                                                            .subscribeOn(Schedulers.io())
//                                                            .observeOn(AndroidSchedulers.mainThread())
//                                                            .subscribe(messageResponse -> {
//                                                                System.out.println("Status message -> " + messageResponse.getMessage());
//                                                                Toast.makeText(getContext(), "Kayıt başarılı", Toast.LENGTH_SHORT).show();
//                                                                goToInfo();
//                                                            },
//                                                                    throwable -> {
//                                                                        if (throwable instanceof HttpException) {
//                                                                            HttpException httpException = (HttpException) throwable;
//                                                                            try {
//                                                                                String errorBody = httpException.response()
//                                                                                        .errorBody()
//                                                                                        .string();
//                                                                                JSONObject jsonObject = new JSONObject(errorBody);
//                                                                                String message = jsonObject.getString("message");
//                                                                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
//                                                                            } catch (Exception e) {
//                                                                                Toast.makeText(getContext(), "Bilinmeyen hata", Toast.LENGTH_LONG).show();
//                                                                            }
//                                                                        } else {
//                                                                            Toast.makeText(getContext(), "Sunucuya bağlanılamadı", Toast.LENGTH_LONG).show();
//                                                                        }
//                                                                        throwable.printStackTrace();
//                                                                    })
//                                            );
//                                        } else {
//                                            Toast.makeText(getContext(), "Girilen kod uyuşmuyor", Toast.LENGTH_SHORT).show();
//                                            return;
//                                        }
//                                    })
//                                    .setNegativeButton("İptal", null)
//                                    .show();
//                        },
//                                throwable -> {
//                                    if (throwable instanceof HttpException) {
//                                        HttpException httpException = (HttpException) throwable;
//                                        try {
//                                            String errorBody = httpException.response()
//                                                    .errorBody()
//                                                    .string();
//                                            JSONObject jsonObject = new JSONObject(errorBody);
//                                            String message = jsonObject.getString("message");
//                                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
//                                        } catch (Exception e) {
//                                            Toast.makeText(getContext(), "Bilinmeyen hata", Toast.LENGTH_LONG).show();
//                                        }
//                                    } else {
//                                        Toast.makeText(getContext(), "Sunucuya bağlanılamadı", Toast.LENGTH_LONG).show();
//                                    }
//                                    throwable.printStackTrace();
//                                })
//        );
    }

    public void registerX() {
        String account = accountEditText.getText().toString();
        String fullname = fullnameEditText.getText().toString();
        String mobile = mobileEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        System.out.println(account + " " + fullname + " " + mobile + " " + password);
        Forgot temporary = new Forgot(account, password, password); // Aktivasyon için
        compositeDisposable.add(
                accountAPI.activateApp(temporary)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                                    Toast.makeText(getContext(), "Aktivasyon kodu gönderildi", Toast.LENGTH_SHORT).show();

                                    LayoutInflater inflater = LayoutInflater.from(getContext());
                                    View dialogView = inflater.inflate(R.layout.dialog, null);
                                    EditText codeEditText = dialogView.findViewById(R.id.codeEditText);
                                    new AlertDialog.Builder(getContext())
                                            .setTitle("Doğrulama Kodu")
                                            .setView(dialogView)
                                            .setPositiveButton("Onayla", (dialog, which) -> {
                                                String code = codeEditText.getText().toString();
                                                if (code.length() != 6) {
                                                    Toast.makeText(getContext(), "Kod 6 haneli olmalı", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                Register request = new Register(account, fullname, mobile, password, code); // Aktivasyon sonrası kayıt için
                                                compositeDisposable.add(
                                                        accountAPI.registerApp(request)
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(messageResponse -> {
                                                                            Toast.makeText(getContext(), "Kayıt başarılı", Toast.LENGTH_SHORT).show();
                                                                            goToInfo();
                                                                        },
                                                                        throwable -> {
                                                                            if (throwable instanceof HttpException) {
                                                                                HttpException httpException = (HttpException) throwable;
                                                                                try {
                                                                                    String errorBody = httpException.response()
                                                                                            .errorBody()
                                                                                            .string();
                                                                                    JSONObject jsonObject = new JSONObject(errorBody);
                                                                                    String message = jsonObject.getString("message");
                                                                                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                                                                                } catch (Exception e) {
                                                                                    Toast.makeText(getContext(), "Bilinmeyen hata", Toast.LENGTH_LONG).show();
                                                                                }
                                                                            } else {
                                                                                Toast.makeText(getContext(), "Sunucuya bağlanılamadı", Toast.LENGTH_LONG).show();
                                                                            }
                                                                            throwable.printStackTrace();
                                                                        })
                                                );
                                            })
                                            .setNegativeButton("İptal", null)
                                            .show();
                                },
                                throwable -> {
                                    if (throwable instanceof HttpException) {
                                        HttpException httpException = (HttpException) throwable;
                                        try {
                                            String errorBody = httpException.response()
                                                    .errorBody()
                                                    .string();
                                            JSONObject jsonObject = new JSONObject(errorBody);
                                            String message = jsonObject.getString("message");
                                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            Toast.makeText(getContext(), "Bilinmeyen hata", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "Sunucuya bağlanılamadı", Toast.LENGTH_LONG).show();
                                    }
                                    throwable.printStackTrace();
                                })
        );
    }


    public void goToLogin() {
        action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    public void goToInfo() {
        action = RegisterFragmentDirections.actionRegisterFragmentToInfoFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}