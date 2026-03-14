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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.restaurant.model.Forgot;
import com.example.restaurant.R;
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

public class ForgotFragment extends Fragment {
    EditText accountEditText, passwordEditText, confirmEditText;
    Button submitButton;
    AccountAPI accountAPI;
    Retrofit retrofit;
    CompositeDisposable compositeDisposable;
    NavDirections action;
    TextWatcher textWatcher;
    private String BASE_URL = "https://ahmet16.pythonanywhere.com/";

    public ForgotFragment() {
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
        // return inflater.inflate(R.layout.fragment_forgot, container, false);
        View view = inflater.inflate(R.layout.fragment_forgot, container, false);
        accountEditText = view.findViewById(R.id.account);
        passwordEditText = view.findViewById(R.id.password);
        confirmEditText = view.findViewById(R.id.confirm);
        submitButton = view.findViewById(R.id.submitButton);
        textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String account = accountEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirm = confirmEditText.getText().toString().trim();
                boolean accountValid = true;
                boolean passwordValid = true;
                boolean confirmValid = true;

                if (account.isEmpty()) {
                    accountEditText.setError("Kullanıcı adı boş olamaz");
                    accountValid = false;
                } else {
                    accountEditText.setError(null);
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
                if (confirm.isEmpty()) {
                    confirmEditText.setError("Şifre tekrarı boş olamaz");
                    confirmValid = false;
                } else if (!confirm.equals(password)) {
                    confirmEditText.setError("Şifreler uyuşmuyor");
                    confirmValid = false;
                } else {
                    confirmEditText.setError(null);
                }
                submitButton.setEnabled(accountValid && passwordValid && confirmValid);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        accountEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        confirmEditText.addTextChangedListener(textWatcher);
        submitButton.setEnabled(false);
        submitButton.setOnClickListener(v -> submitX());
        return view;
    }

    public void submit() {
//        String account = accountEditText.getText().toString();
//        String password = passwordEditText.getText().toString();
//        String confirm = confirmEditText.getText().toString();
//        System.out.println(account + " " + password + " " + confirm);
//        Forgot request = new Forgot(account, password, confirm);
//        compositeDisposable.add(
//                accountAPI.activateApp(request)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(response -> {
//                                    System.out.println("Status code: " + response.message);
//                                    String dbCode = response.activation_code;
//                                    Toast.makeText(getContext(), "Aktivasyon kodu gönderildi", Toast.LENGTH_SHORT).show();
//
//                                    LayoutInflater inflater = LayoutInflater.from(getContext());
//                                    View dialogView = inflater.inflate(R.layout.dialog, null);
//                                    EditText codeEditText = dialogView.findViewById(R.id.codeEditText);
//
//                                    new AlertDialog.Builder(getContext())
//                                            .setTitle("Doğrulama Kodu")
//                                            .setView(dialogView)
//                                            .setPositiveButton("Onayla", (dialog, which) -> {
//                                                String code = codeEditText.getText().toString();
//                                                if (code.length() != 6) {
//                                                    Toast.makeText(getContext(), "Lütfen 6 haneli kod girin", Toast.LENGTH_SHORT).show();
//                                                    return;
//                                                }
//                                                System.out.println("Girilen kod: " + code + " veritabanı kodu: " + dbCode);
//                                                if (dbCode.equals(code)) {
//                                                    compositeDisposable.add(
//                                                            accountAPI.forgotApp(request)
//                                                                    .subscribeOn(Schedulers.io())
//                                                                    .observeOn(AndroidSchedulers.mainThread())
//                                                                    .subscribe(messageResponse -> {
//                                                                                System.out.println("Status code: " + messageResponse.getMessage());
//                                                                                Toast.makeText(getContext(),"Şifre değiştirildi",Toast.LENGTH_LONG).show();
//                                                                                goToLogin();
//                                                                            },
//                                                                            throwable -> {
//                                                                                if (throwable instanceof HttpException) {
//                                                                                    HttpException httpException = (HttpException) throwable;
//                                                                                    try {
//                                                                                        String errorBody = httpException.response()
//                                                                                                .errorBody()
//                                                                                                .string();
//                                                                                        JSONObject jsonObject = new JSONObject(errorBody);
//                                                                                        String message = jsonObject.getString("message");
//                                                                                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
//                                                                                    } catch (Exception e) {
//                                                                                        Toast.makeText(getContext(), "Bilinmeyen hata", Toast.LENGTH_LONG).show();
//                                                                                    }
//                                                                                } else {
//                                                                                    Toast.makeText(getContext(), "Sunucuya bağlanılamadı", Toast.LENGTH_LONG).show();
//                                                                                }
//                                                                                throwable.printStackTrace();
//                                                                            })
//                                                    );
//                                                } else {
//                                                    Toast.makeText(getContext(), "Girilen kod uyuşmuyor", Toast.LENGTH_SHORT).show();
//                                                }
//                                            })
//                                            .setNegativeButton("İptal", null)
//                                            .show();
//
//                                },
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

    public void submitX() {
        String account = accountEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirm = confirmEditText.getText().toString();
        System.out.println(account + " " + password + " " + confirm);
        Forgot request = new Forgot(account, password, confirm);
        compositeDisposable.add(
                accountAPI.activateApp(request)
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
                                                    Toast.makeText(getContext(), "Lütfen 6 haneli kod girin", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                compositeDisposable.add(
                                                        accountAPI.forgotApp(request)
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(messageResponse -> {
                                                                            System.out.println("Status code: " + messageResponse.getMessage());
                                                                            Toast.makeText(getContext(), "Şifre değiştirildi", Toast.LENGTH_LONG).show();
                                                                            goToLogin();
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
        action = ForgotFragmentDirections.actionForgotFragmentToLoginFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}