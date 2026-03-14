package com.example.restaurant.model;

import com.google.gson.annotations.SerializedName;

public class Features {
    @SerializedName("id")
    public String id;
    @SerializedName("urun_ad")
    public String urun_ad;
    @SerializedName("KategoriIsim")
    public String KategoriIsim;
    @SerializedName("max")
    public String max;
    @SerializedName("min")
    public String min;
    @SerializedName("br")
    public String br;
}
