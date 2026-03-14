package com.example.restaurant.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.restaurant.adapter.BasketAdapter;
import com.example.restaurant.model.Features;
import com.example.restaurant.R;
import com.example.restaurant.model.BasketResponse;
import com.example.restaurant.model.Market;
import com.example.restaurant.network.AccountAPI;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class BasketDialogFragment extends BottomSheetDialogFragment {
    String token;
    BasketAdapter basketAdapter;
    RecyclerView recyclerView;
    ArrayList<Features> basketModels;
    AccountAPI accountAPI;
    Retrofit retrofit;
    CompositeDisposable compositeDisposable;
    NavDirections action;
    private String BASE_URL = "https://ahmet16.pythonanywhere.com/";

    public BasketDialogFragment(String token) {
        this.token = token;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            token = getArguments().getString(token);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (token == null || token.isEmpty()) {
            goToLogin();
        }
        compositeDisposable = new CompositeDisposable();
        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        accountAPI = retrofit.create(AccountAPI.class);
        loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_basket_dialog, container, false);
        View view = inflater.inflate(R.layout.fragment_basket_dialog, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewTextView);
        return view;
    }

    public void loadData() {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(accountAPI.basketApp("Bearer " + token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse, throwable -> {
                    if (throwable instanceof HttpException) {
                        HttpException httpException = (HttpException) throwable;

                        if (httpException.code() == 401) {
                            Toast.makeText(getContext(), "Oturum süresi doldu", Toast.LENGTH_LONG).show();
                            goToLogin();
                            return;
                        }
                    }

                    Toast.makeText(getContext(), "Veriler yüklenemedi", Toast.LENGTH_LONG).show();
                    throwable.printStackTrace();
                }));
    }

    public void handleResponse(Market market) {
        basketModels = new ArrayList<>(market.market);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        basketAdapter = new BasketAdapter(basketModels, product -> accountAPI.basketDel("Bearer " + token, new BasketResponse(product.id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(market1 -> {
                    Toast.makeText(getContext(), product.urun_ad + " sepetten çıkarıldı", Toast.LENGTH_SHORT).show();
                    loadData();
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
                ));
        recyclerView.setAdapter(basketAdapter);
    }

    public void goToLogin() {
        action = HomeFragmentDirections.actionHomeFragmentToLoginFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}