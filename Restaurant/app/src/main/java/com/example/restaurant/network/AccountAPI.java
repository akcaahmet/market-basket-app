package com.example.restaurant.network;

import android.os.Message;

import com.example.restaurant.model.ActivateResponse;
import com.example.restaurant.model.Forgot;
import com.example.restaurant.model.Account;
import com.example.restaurant.model.BasketResponse;
import com.example.restaurant.model.LoginGoogle;
import com.example.restaurant.model.LoginResponse;
import com.example.restaurant.model.Market;
import com.example.restaurant.model.MessageResponse;
import com.example.restaurant.model.Register;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AccountAPI {
    @POST("login")
    Observable<LoginResponse> loginApp(@Body Account request);

    @POST("register")
    Observable<MessageResponse> registerApp(@Body Register request);

    @POST("login-google")
    Observable<LoginResponse> loginGoogleApp(@Body LoginGoogle request);

    @POST("activate")
    Observable<MessageResponse> activateApp(@Body Forgot request);

    @PATCH("forgot")
    Observable<MessageResponse> forgotApp(@Body Forgot request);

    @GET("market")
    Observable<Market> marketApp();

    @GET("basket")
    Observable<Market> basketApp(@Header("Authorization") String token);

    @POST("basket")
    Observable<Market> basketAdd(@Header("Authorization") String token, @Body BasketResponse request);

    @HTTP(method = "DELETE", path = "basket", hasBody = true)
    Observable<Market> basketDel(@Header("Authorization") String token, @Body BasketResponse request);
}
