package com.example.cookingingredients;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingingredients.adapter.MealAdapter;
import com.example.cookingingredients.data.remote.ApiService;
import com.example.cookingingredients.data.remote.MealSummary;
import com.example.cookingingredients.data.remote.MealSummaryResponse;
import com.example.cookingingredients.data.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvFeaturedMeals;
    private MealAdapter mealAdapter;
    private List<MealSummary> mealList;

    // Mục "Món gợi ý hôm nay"
    private MealAdapter todayAdapter;
    private final List<MealSummary> todayList = new ArrayList<>();

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo API
        apiService = RetrofitClient.getApiService();

        // Danh sách "Công thức nổi bật" (cuộn ngang)
        rvFeaturedMeals = findViewById(R.id.rvFeaturedMeals);
        mealList = new ArrayList<>();
        rvFeaturedMeals.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mealAdapter = new MealAdapter(mealList);
        rvFeaturedMeals.setAdapter(mealAdapter);

        // Danh sách "Món gợi ý hôm nay" (lưới 2 cột)
        RecyclerView rvToday = findViewById(R.id.rvTodayMeals);
        rvToday.setLayoutManager(new GridLayoutManager(this, 2));
        todayAdapter = new MealAdapter(todayList);
        todayAdapter.setFullWidth(true);
        rvToday.setAdapter(todayAdapter);

        // Chuyển màn
        View.OnClickListener openSelect = v ->
                startActivity(new Intent(HomeActivity.this, SelectIngredientsActivity.class));
        findViewById(R.id.navIngredients).setOnClickListener(openSelect);
        findViewById(R.id.tvSeeMoreIngredients).setOnClickListener(openSelect);
        findViewById(R.id.btnBannerStart).setOnClickListener(openSelect);
        findViewById(R.id.navFavorites).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, FavoritesActivity.class)));
        findViewById(R.id.navHistory).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, HistoryActivity.class)));

        // Ô tìm kiếm: dịch từ khóa tiếng Việt sang tiếng Anh rồi mở màn Kết quả
        EditText edtSearch = findViewById(R.id.edtSearch);
        Translator viEnTranslator = Translation.getClient(new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.VIETNAMESE)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build());
        getLifecycle().addObserver(viEnTranslator);

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            String q = edtSearch.getText().toString().trim();
            if (q.isEmpty()) return true;

            if (isAscii(q)) {
                openSearch(q);
                return true;
            }

            Toast.makeText(this, "Đang dịch từ khóa...", Toast.LENGTH_SHORT).show();
            viEnTranslator.downloadModelIfNeeded(new DownloadConditions.Builder().build())
                    .addOnSuccessListener(unused -> viEnTranslator.translate(q)
                            .addOnSuccessListener(en -> {
                                String query = en.trim().toLowerCase().replaceAll("[.!?]+$", "");
                                openSearch(query.isEmpty() ? q : query);
                            })
                            .addOnFailureListener(e -> openSearch(q)))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Chưa dịch được, tìm bằng từ gốc", Toast.LENGTH_SHORT).show();
                        openSearch(q);
                    });
            return true;
        });

        // Gọi API
        loadFeaturedMeals();
        loadTodayMeals();
    }

    private void openSearch(String query) {
        Intent intent = new Intent(HomeActivity.this, SearchResultActivity.class);
        intent.putExtra("query", query);
        startActivity(intent);
    }

    private boolean isAscii(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 127) return false;
        }
        return true;
    }

    private void loadFeaturedMeals() {
        apiService.searchMeals("chicken")
                .enqueue(new Callback<MealSummaryResponse>() {
                    @Override
                    public void onResponse(Call<MealSummaryResponse> call,
                                           Response<MealSummaryResponse> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().meals != null) {
                            mealList.clear();
                            mealList.addAll(response.body().meals);
                            mealAdapter.notifyDataSetChanged();
                            Log.d("API_MEAL", "Đã tải: " + mealList.size() + " món ăn");
                        } else {
                            Toast.makeText(HomeActivity.this,
                                    "Không tìm thấy món ăn!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MealSummaryResponse> call, Throwable t) {
                        Log.e("API_MEAL", "Lỗi kết nối: " + t.getMessage());
                        Toast.makeText(HomeActivity.this,
                                "Không thể kết nối đến máy chủ!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTodayMeals() {
        apiService.searchMeals("beef")
                .enqueue(new Callback<MealSummaryResponse>() {
                    @Override
                    public void onResponse(Call<MealSummaryResponse> call,
                                           Response<MealSummaryResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().meals != null) {
                            List<MealSummary> all = response.body().meals;
                            todayList.clear();
                            todayList.addAll(all.subList(0, Math.min(6, all.size())));
                            todayAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<MealSummaryResponse> call, Throwable t) {
                        Log.e("API_MEAL", "Lỗi tải món hôm nay: " + t.getMessage());
                    }
                });
    }
}