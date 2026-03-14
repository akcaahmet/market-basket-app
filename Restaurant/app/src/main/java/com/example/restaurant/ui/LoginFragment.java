package com.example.restaurant.ui;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.restaurant.model.LoginGoogle;
import com.example.restaurant.model.Register;
import com.example.restaurant.ui.LoginFragmentDirections;
import com.example.restaurant.R;
import com.example.restaurant.model.Account;
import com.example.restaurant.network.AccountAPI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
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

public class LoginFragment extends Fragment {
    /*
    Kullanıcı adı ve şifre alınacak. Sunucudan veri tabanı kontrolü yapılacak giriş işlemi ona göre gerçekleştirilecek.
    Yada şimdilik geçici olması sebebiyle basit birşey yapılabilir.
     */
    EditText accountEditText;
    EditText passwordEditText;
    Button loginButton;
    AccountAPI accountAPI;
    Retrofit retrofit;
    CompositeDisposable compositeDisposable;
    NavDirections action;
    TextView registerButton, forgotButton;
    TextWatcher textWatcher;
    private static final int RC_SIGN_IN = 200;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton signInButton;
    CheckBox checkBoxButton;
    SharedPreferences sharedPreferences;
    String savedAccount, savedPassword;
    private String BASE_URL = "https://ahmet16.pythonanywhere.com/";

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getContext().getSharedPreferences("com.example.restaurant", Context.MODE_PRIVATE);
    }

    @Override
    public void onStart() {
        super.onStart();
        savedAccount = sharedPreferences.getString("account", null);
        savedPassword = sharedPreferences.getString("password", null);
        if (savedAccount != null && savedPassword != null){
            Account request = new Account(savedAccount, savedPassword);
            compositeDisposable.add(
                    accountAPI.loginApp(request)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(response -> {
                                String token = response.getToken();
                                Toast.makeText(getContext(), "Giriş başarılı", Toast.LENGTH_SHORT).show();
                                goToHome(token);
                            },
                                    throwable -> {
                                        throwable.printStackTrace();
                                    })
            );
        }
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
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("12421091826-s5k138cnran0jk001hhse78lnt8iau3r.apps.googleusercontent.com")
                // .requestScopes(new Scope("https://www.googleapis.com/auth/user.phonenumbers.read"))
                // .requestServerAuthCode("12421091826-s5k138cnran0jk001hhse78lnt8iau3r.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_login, container, false);
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        accountEditText = view.findViewById(R.id.account);
        passwordEditText = view.findViewById(R.id.password);
        loginButton = view.findViewById(R.id.loginButton);
        textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String username = accountEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                boolean usernameValid = true;
                boolean passwordValid = true;

                if (username.isEmpty()) {
                    accountEditText.setError("Kullanıcı adı boş olamaz");
                    usernameValid = false;
                } else if (username.length() < 5) {
                    accountEditText.setError("Kullanıcı adı en az 5 karakter olmalı");
                    usernameValid = false;
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
                loginButton.setEnabled(usernameValid && passwordValid);
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
        loginButton.setEnabled(false);
        loginButton.setOnClickListener(v -> login()
            /*
            {
            String account = accountEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            System.out.println(account + " "+ password);
            }
             */
        );
        registerButton = view.findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> goToRegister());
        forgotButton = view.findViewById(R.id.forgotButton);
        forgotButton.setOnClickListener(v -> goToForgot());
        signInButton = view.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(v -> signIn());
        checkBoxButton = view.findViewById(R.id.checkboxButton);
        return view;
    }

    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            //updateUI(account);
            LoginGoogle request = new LoginGoogle(account.getIdToken());
            compositeDisposable.add(
                    accountAPI.loginGoogleApp(request)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(response -> {
                                String token = response.getToken();
                                        System.out.println(token);
                                Toast.makeText(getContext(), "Giriş başarılı", Toast.LENGTH_SHORT).show();
                                goToHome(token);
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
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
            System.out.println(e.getStatusCode());
        }
    }

    public void login() {
        // Boşluk temizlemek için trim eklenebilir.
        String account = accountEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        System.out.println(account + " " + password);
        Account request = new Account(account, password);
        compositeDisposable.add(
                accountAPI.loginApp(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                                    String token = response.getToken();
                                    System.out.println("Status code: " + response.getMessage());
                                    Toast.makeText(getContext(), "Giriş başarılı", Toast.LENGTH_LONG).show();
                                    if (checkBoxButton.isChecked()) {
                                        sharedPreferences.edit().putString("account", account).putString("password", password).apply();
                                    }
                                    goToHome(token);
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
                                }
                        )
        );
    }

    public void goToForgot() {
        action = LoginFragmentDirections.actionLoginFragmentToForgotFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    public void goToRegister() {
        action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    public void goToHome(String token) {
        // action = LoginFragmentDirections.actionLoginFragmentToHomeFragment();
        LoginFragmentDirections.ActionLoginFragmentToHomeFragment action = LoginFragmentDirections.actionLoginFragmentToHomeFragment(token);
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}