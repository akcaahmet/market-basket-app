package com.example.restaurant.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.restaurant.model.Features;
import com.example.restaurant.ui.HomeFragmentArgs;
import com.example.restaurant.adapter.ProductAdapter;
import com.example.restaurant.R;
import com.example.restaurant.model.BasketResponse;
import com.example.restaurant.model.Market;
import com.example.restaurant.network.AccountAPI;
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

public class HomeFragment extends Fragment {
    EditText searchEditText;
    ProductAdapter productAdapter;
    RecyclerView recyclerView;
    ImageButton basketButton, logoutButton;
    ArrayList<Features> marketModels;
    AccountAPI accountAPI;
    Retrofit retrofit;
    CompositeDisposable compositeDisposable;
    NavDirections action;
    SharedPreferences sharedPreferences;
    String token;
    ArrayList<Features> allProducts;
    private String BASE_URL = "https://ahmet16.pythonanywhere.com/";

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getContext().getSharedPreferences("com.example.restaurant", Context.MODE_PRIVATE);
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
        if (getArguments() != null) {
            token = HomeFragmentArgs.fromBundle(getArguments()).getToken();
        }
        loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_home, container, false);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewTextView);
        searchEditText = view.findViewById(R.id.search);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (allProducts == null) return;
                String searchText = s.toString().toLowerCase();
                marketModels.clear();
                if (searchText.isEmpty()) {
                    marketModels.addAll(allProducts);
                } else {
                    for (Features item : allProducts) {
                        if (item.urun_ad.toLowerCase().contains(searchText)) {
                            marketModels.add(item);
                        }
                    }
                }
                productAdapter.notifyDataSetChanged();
            }
        });
        basketButton = view.findViewById(R.id.basketButton);
        basketButton.setOnClickListener(v -> goToBasket());
        logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> goToLogin());
        return view;
    }

    public void loadData() {
        compositeDisposable.add(
                accountAPI.marketApp()
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
                        })
        );
    }

    private void handleResponse(Market market) {
        allProducts = new ArrayList<>(market.market);
        marketModels = new ArrayList<>(allProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productAdapter = new ProductAdapter(marketModels, product ->
                compositeDisposable.add(
                        accountAPI.basketAdd("Bearer " + token, new BasketResponse(product.id))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(market1 -> {
                                    Toast.makeText(getContext(), product.urun_ad + " sepete eklendi", Toast.LENGTH_SHORT).show();
                                }, throwable -> {
                                    if (throwable instanceof HttpException) {
                                        HttpException httpException = (HttpException) throwable;
                                        try {
                                            String errorBody = httpException.response().errorBody().string();
                                            JSONObject jsonObject = new JSONObject(errorBody);
                                            String message = jsonObject.getString("message");
                                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Toast.makeText(getContext(), "Bilinmeyen hata", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        throwable.printStackTrace();
                                    }
                                })
                ));
        recyclerView.setAdapter(productAdapter);
    }

    public void goToBasket() {
        BasketDialogFragment basketDialogFragment = new BasketDialogFragment(token);
        basketDialogFragment.show(getParentFragmentManager(), "Basket Dialog");
    }

    public void goToLogin() {
        action = HomeFragmentDirections.actionHomeFragmentToLoginFragment();
        sharedPreferences.edit().remove("account").remove("password").apply();
        Navigation.findNavController(requireView()).navigate(action);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}