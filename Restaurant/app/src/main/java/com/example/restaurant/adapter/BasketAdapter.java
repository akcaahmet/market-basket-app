package com.example.restaurant.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurant.model.Features;
import com.example.restaurant.databinding.BasketRowBinding;

import java.util.ArrayList;

public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.BasketHolder> {
    ArrayList<Features> basketArrayList;
    private OnDelBasketClickListener listener;

    public static interface OnDelBasketClickListener {
        void onDelBasketClick(Features product);
    }

    public BasketAdapter(ArrayList<Features> basketArrayList, OnDelBasketClickListener listener) {
        this.basketArrayList = basketArrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BasketHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BasketRowBinding basketRowBinding = BasketRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BasketHolder(basketRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BasketHolder holder, int position) {
        holder.binding.urunAdi.setText(basketArrayList.get(position).urun_ad);
        holder.binding.kategori.setText(basketArrayList.get(position).KategoriIsim);
        holder.binding.maksimum.setText(basketArrayList.get(position).max);
        holder.binding.minimum.setText(basketArrayList.get(position).min);
        holder.binding.birim.setText(basketArrayList.get(position).br);
        Features model = basketArrayList.get(position);
        holder.binding.deleteBasket.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelBasketClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return basketArrayList.size();
    }

    public class BasketHolder extends RecyclerView.ViewHolder {
        private BasketRowBinding binding;

        public BasketHolder(BasketRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
