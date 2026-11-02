package com.example.cookingingredients;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingingredients.adapter.IngredientAdapter;
import com.example.cookingingredients.model.Ingredient;
import com.example.cookingingredients.data.local.HistoryManager;
import com.example.cookingingredients.model.IngredientCatalog;

import java.util.ArrayList;
import java.util.List;

public class SelectIngredientsActivity extends AppCompatActivity {

    private final List<Ingredient> ingredients = new ArrayList<>();
    private Button btnFind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_ingredients);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnFind = findViewById(R.id.btnFind);

        ingredients.addAll(IngredientCatalog.createAll());

        RecyclerView rv = findViewById(R.id.rvIngredients);
        rv.setLayoutManager(new GridLayoutManager(this, 4));
        rv.setAdapter(new IngredientAdapter(ingredients, this::updateButton));

        btnFind.setOnClickListener(v -> {
            ArrayList<String> selected = new ArrayList<>();
            for (Ingredient i : ingredients) if (i.selected) selected.add(i.apiName);
            new HistoryManager(this).add(selected);
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putStringArrayListExtra("ingredients", selected);
            startActivity(intent);
        });
        updateButton(0);
    }

    private void updateButton(int count) {
        btnFind.setText("Tìm công thức (" + count + " nguyên liệu)");
        btnFind.setEnabled(count > 0);
        int color = count > 0
                ? ContextCompat.getColor(this, R.color.primary)
                : 0xFFBDBDBD;
        btnFind.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}