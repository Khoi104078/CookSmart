package com.example.cookingingredients;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.cookingingredients.adapter.MealAdapter;
import com.example.cookingingredients.data.local.NetworkUtils;
import com.example.cookingingredients.data.remote.ApiService;
import com.example.cookingingredients.data.remote.MealSummary;
import com.example.cookingingredients.data.remote.MealSummaryResponse;
import com.example.cookingingredients.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    private final List<MealSummary> mealList = new ArrayList<>();
    private final Map<String, Integer> matchCount = new HashMap<>();
    private final Map<String, MealSummary> mealById = new HashMap<>();

    private MealAdapter mealAdapter;
    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvSubtitle, tvEmpty;
    private View layoutOffline;

    private ArrayList<String> ingredients;
    private int pending;
    private int failed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        progress = findViewById(R.id.progress);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvEmpty = findViewById(R.id.tvEmpty);
        layoutOffline = findViewById(R.id.layoutOffline);
        findViewById(R.id.btnRetry).setOnClickListener(v -> load());

        rv = findViewById(R.id.rvResults);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        mealAdapter = new MealAdapter(mealList);
        mealAdapter.setFullWidth(true);
        rv.setAdapter(mealAdapter);

        ingredients = getIntent().getStringArrayListExtra("ingredients");
        if (ingredients == null || ingredients.isEmpty()) {
            finish();
            return;
        }
        tvSubtitle.setText("Dựa trên " + ingredients.size() + " nguyên liệu bạn đã chọn");
        load();
    }

    private void load() {
        layoutOffline.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        if (!NetworkUtils.isOnline(this)) {
            showOffline();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        rv.setVisibility(View.VISIBLE);
        matchCount.clear();
        mealById.clear();
        mealList.clear();
        mealAdapter.notifyDataSetChanged();

        pending = ingredients.size();
        failed = 0;

        ApiService api = RetrofitClient.getApiService();
        for (String ing : ingredients) {
            api.filterByIngredient(ing).enqueue(new Callback<MealSummaryResponse>() {
                @Override
                public void onResponse(Call<MealSummaryResponse> call,
                                       Response<MealSummaryResponse> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().meals != null) {
                        for (MealSummary m : response.body().meals) {
                            String id = m.getIdMeal();
                            Integer c = matchCount.get(id);
                            matchCount.put(id, c == null ? 1 : c + 1);
                            mealById.put(id, m);
                        }
                    }
                    done();
                }

                @Override
                public void onFailure(Call<MealSummaryResponse> call, Throwable t) {
                    failed++;
                    done();
                }
            });
        }
    }

    private void showOffline() {
        progress.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);
        layoutOffline.setVisibility(View.VISIBLE);
    }

    private void done() {
        pending--;
        if (pending > 0) return;

        // Tất cả request đều lỗi kết nối -> hiện màn Mất mạng
        if (failed == ingredients.size()) {
            showOffline();
            return;
        }

        progress.setVisibility(View.GONE);
        mealList.clear();
        mealList.addAll(mealById.values());
        Collections.sort(mealList, (a, b) ->
                matchCount.get(b.getIdMeal()) - matchCount.get(a.getIdMeal()));
        mealAdapter.setMatchInfo(matchCount, ingredients.size());
        mealAdapter.notifyDataSetChanged();
        mealAdapter.notifyDataSetChanged();

        tvEmpty.setVisibility(mealList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}