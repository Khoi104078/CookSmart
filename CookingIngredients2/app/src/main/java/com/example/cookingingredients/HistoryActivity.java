package com.example.cookingingredients;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingingredients.model.IngredientCatalog;
import com.example.cookingingredients.data.local.HistoryManager;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private final List<List<String>> items = new ArrayList<>();
    private HistoryManager history;
    private HistoryAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        history = new HistoryManager(this);
        tvEmpty = findViewById(R.id.tvEmpty);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            history.clear();
            reload();
        });

        RecyclerView rv = findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter();
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        items.clear();
        items.addAll(history.getAll());
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            List<String> apiNames = items.get(position);
            List<String> labels = new ArrayList<>();
            for (String name : apiNames) labels.add(IngredientCatalog.label(name));
            h.tvText.setText(String.join("  +  ", labels));
            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HistoryActivity.this, ResultActivity.class);
                intent.putStringArrayListExtra("ingredients", new ArrayList<>(apiNames));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvText;
            VH(View v) {
                super(v);
                tvText = v.findViewById(R.id.tvHistoryText);
            }
        }
    }
}