package com.example.restaurant.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.model.Features;
import com.example.restaurant.databinding.RowBinding;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductHolder> {
    ArrayList<Features> marketArrayList;
    private OnAddBasketClickListener listener; // callback interface

    public static interface OnAddBasketClickListener {
        void onAddBasketClick(Features product);
    }

    public ProductAdapter(ArrayList<Features> marketArrayList, OnAddBasketClickListener listener) {
        this.marketArrayList = marketArrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowBinding rowBinding = RowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductHolder(rowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductHolder holder, int position) {
        holder.binding.urunAdi.setText(marketArrayList.get(position).urun_ad);
        holder.binding.kategori.setText(marketArrayList.get(position).KategoriIsim);
        holder.binding.maksimum.setText(marketArrayList.get(position).max);
        holder.binding.minimum.setText(marketArrayList.get(position).min);
        holder.binding.birim.setText(marketArrayList.get(position).br);
        Features model = marketArrayList.get(position);
        holder.binding.addBasket.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddBasketClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return marketArrayList.size();
    }

    public class ProductHolder extends RecyclerView.ViewHolder{
        private RowBinding binding;
        public ProductHolder(RowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
