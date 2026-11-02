package com.example.cookingingredients;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingingredients.adapter.MealAdapter;
import com.example.cookingingredients.data.local.NetworkUtils;
import com.example.cookingingredients.data.remote.MealSummary;
import com.example.cookingingredients.data.remote.MealSummaryResponse;
import com.example.cookingingredients.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultActivity extends AppCompatActivity {

    private final List<MealSummary> mealList = new ArrayList<>();
    private MealAdapter adapter;
    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvEmpty;
    private View layoutOffline;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);
        layoutOffline = findViewById(R.id.layoutOffline);
        findViewById(R.id.btnRetry).setOnClickListener(v -> load());

        rv = findViewById(R.id.rvSearch);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MealAdapter(mealList);
        adapter.setFullWidth(true);
        rv.setAdapter(adapter);

        query = getIntent().getStringExtra("query");
        if (query == null || query.trim().isEmpty()) {
            finish();
            return;
        }
        ((TextView) findViewById(R.id.tvSubtitle))
                .setText("Từ khóa: \"" + query + "\"");
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
        mealList.clear();
        adapter.notifyDataSetChanged();

        RetrofitClient.getApiService().searchMeals(query)
                .enqueue(new Callback<MealSummaryResponse>() {
                    @Override
                    public void onResponse(Call<MealSummaryResponse> call,
                                           Response<MealSummaryResponse> response) {
                        progress.setVisibility(View.GONE);
                        // API trả "meals": null khi không có món nào khớp
                        if (response.isSuccessful() && response.body() != null
                                && response.body().meals != null) {
                            mealList.addAll(response.body().meals);
                            adapter.notifyDataSetChanged();
                        }
                        tvEmpty.setVisibility(
                                mealList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(Call<MealSummaryResponse> call, Throwable t) {
                        showOffline();
                    }
                });
    }

    private void showOffline() {
        progress.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);
        layoutOffline.setVisibility(View.VISIBLE);
    }
}