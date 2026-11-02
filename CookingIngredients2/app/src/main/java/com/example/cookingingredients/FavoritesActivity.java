package com.example.cookingingredients;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingingredients.adapter.MealAdapter;
import com.example.cookingingredients.data.local.FavoriteManager;
import com.example.cookingingredients.data.remote.MealSummary;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private final List<MealSummary> mealList = new ArrayList<>();
    private MealAdapter adapter;
    private TextView tvEmpty;
    private FavoriteManager favorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tvEmpty);
        favorites = new FavoriteManager(this);

        RecyclerView rv = findViewById(R.id.rvFavorites);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MealAdapter(mealList);
        adapter.setFullWidth(true);
        rv.setAdapter(adapter);
    }

    // Quay lại từ trang Chi tiết (đã bỏ tim) thì danh sách tự cập nhật
    @Override
    protected void onResume() {
        super.onResume();
        mealList.clear();
        mealList.addAll(favorites.getAll());
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(mealList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}