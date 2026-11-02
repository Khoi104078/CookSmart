package com.example.cookingingredients.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.graphics.Typeface;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingingredients.R;
import com.example.cookingingredients.model.Ingredient;

import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.VH> {

    public interface OnSelectionChanged { void onChanged(int count); }

    private final List<Ingredient> items;
    private final OnSelectionChanged listener;

    public IngredientAdapter(List<Ingredient> items, OnSelectionChanged listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Ingredient item = items.get(position);
        h.tvEmoji.setText(item.emoji);
        h.tvName.setText(item.name);
        h.tvEmoji.setBackgroundResource(item.selected
                ? R.drawable.bg_ingredient_selected
                : R.drawable.bg_ingredient_normal);
        h.tvName.setTextColor(ContextCompat.getColor(h.itemView.getContext(),
                item.selected ? R.color.primary : R.color.text_primary));
        h.tvName.setTypeface(null, item.selected ? Typeface.BOLD : Typeface.NORMAL);

        h.itemView.setOnClickListener(v -> {
            item.selected = !item.selected;
            notifyItemChanged(h.getBindingAdapterPosition());
            listener.onChanged(countSelected());
        });
    }

    private int countSelected() {
        int c = 0;
        for (Ingredient i : items) if (i.selected) c++;
        return c;
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName;
        VH(View v) {
            super(v);
            tvEmoji = v.findViewById(R.id.tvEmoji);
            tvName = v.findViewById(R.id.tvName);
        }
    }
}