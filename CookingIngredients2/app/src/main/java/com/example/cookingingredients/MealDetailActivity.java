package com.example.cookingingredients;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cookingingredients.data.local.FavoriteManager;
import com.example.cookingingredients.data.local.NetworkUtils;
import com.example.cookingingredients.data.remote.MealDetail;
import com.example.cookingingredients.data.remote.MealDetailResponse;
import com.example.cookingingredients.data.remote.MealSummary;
import com.example.cookingingredients.data.remote.RetrofitClient;
import com.example.cookingingredients.data.local.MealCache;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;

public class MealDetailActivity extends AppCompatActivity {

    private ImageView imgMeal;
    private TextView tvName, tvInfo, tvIngredients, tvInstructions;
    private ProgressBar progress;
    private TextView btnFavorite;
    private View scrollContent, layoutOffline;
    private FavoriteManager favorites;
    private MealDetail currentMeal;
    private String mealId;

    private MealCache cache;

    private Translator translator;
    private TextView tvTranslateStatus, btnLang;
    private boolean showVietnamese = false;
    private String enName, enIngredients, enInstructions;
    private String viName, viIngredients, viInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        imgMeal = findViewById(R.id.imgMeal);
        tvName = findViewById(R.id.tvName);
        tvInfo = findViewById(R.id.tvInfo);
        tvIngredients = findViewById(R.id.tvIngredients);
        tvInstructions = findViewById(R.id.tvInstructions);
        progress = findViewById(R.id.progress);
        scrollContent = findViewById(R.id.scrollContent);
        layoutOffline = findViewById(R.id.layoutOffline);
        findViewById(R.id.btnRetry).setOnClickListener(v -> loadDetail());
        tvTranslateStatus = findViewById(R.id.tvTranslateStatus);
        btnLang = findViewById(R.id.btnLang);
        btnLang.setOnClickListener(v -> applyLanguage(!showVietnamese));

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                .build();
        translator = Translation.getClient(options);
        getLifecycle().addObserver(translator);

        favorites = new FavoriteManager(this);
        cache = new MealCache(this);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnFavorite.setOnClickListener(v -> {
            if (currentMeal == null) return;
            boolean isFav = favorites.toggle(new MealSummary(
                    currentMeal.idMeal, currentMeal.strMeal, currentMeal.strMealThumb));
            updateHeart(isFav);
            Toast.makeText(this, isFav ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích",
                    Toast.LENGTH_SHORT).show();
        });

        mealId = getIntent().getStringExtra("mealId");
        if (mealId == null) {
            finish();
            return;
        }
        loadDetail();
    }

    private void loadDetail() {
        layoutOffline.setVisibility(View.GONE);

        if (!NetworkUtils.isOnline(this)) {
            MealDetail cached = cache.getDetail(mealId);
            if (cached != null) {
                progress.setVisibility(View.GONE);
                show(cached);
            } else {
                showOffline();
            }
            return;
        }

        progress.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getMealDetail(mealId)
                .enqueue(new Callback<MealDetailResponse>() {
                    @Override
                    public void onResponse(Call<MealDetailResponse> call,
                                           Response<MealDetailResponse> response) {
                        progress.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().meals != null
                                && !response.body().meals.isEmpty()) {
                            MealDetail loaded = response.body().meals.get(0);
                            cache.saveDetail(loaded);
                            show(loaded);
                        } else {
                            Toast.makeText(MealDetailActivity.this,
                                    "Không tải được công thức", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MealDetailResponse> call, Throwable t) {
                        showOffline();
                    }
                });
    }

    private void showOffline() {
        progress.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);
        layoutOffline.setVisibility(View.VISIBLE);
    }

    private void show(MealDetail meal) {
        currentMeal = meal;
        scrollContent.setVisibility(View.VISIBLE);
        updateHeart(favorites.isFavorite(meal.idMeal));
        tvInfo.setText(meal.strCategory + " • " + meal.strArea);
        Glide.with(this).load(meal.strMealThumb).centerCrop().into(imgMeal);

        // Bản gốc tiếng Anh
        enName = meal.strMeal;
        enIngredients = String.join("\n", meal.getIngredientLines());
        enInstructions = meal.strInstructions;
        showVietnamese = false;
        tvName.setText(enName);
        tvIngredients.setText(enIngredients);
        tvInstructions.setText(enInstructions);

        String[] saved = cache.getTranslation(meal.idMeal);
        if (saved != null) {
            viName = saved[0];
            viIngredients = saved[1];
            viInstructions = saved[2];
            tvTranslateStatus.setVisibility(View.GONE);
            applyLanguage(true);
        } else {
            startTranslate(meal);
        }
    }

    private void startTranslate(MealDetail meal) {
        btnLang.setVisibility(View.GONE);
        tvTranslateStatus.setText("Đang dịch sang tiếng Việt...");
        tvTranslateStatus.setVisibility(View.VISIBLE);

        translator.downloadModelIfNeeded(new DownloadConditions.Builder().build())
                .addOnSuccessListener(unused -> doTranslate(meal))
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    tvTranslateStatus.setText(
                            "Chưa dịch được (lần đầu cần mạng để tải mô hình). Đang hiện bản gốc.");
                });
    }

    private void doTranslate(MealDetail meal) {
        List<String> ingLines = meal.getIngredientLines();
        List<String> paragraphs = new ArrayList<>();
        if (meal.strInstructions != null) {
            for (String p : meal.strInstructions.split("\\r?\\n")) {
                if (!p.trim().isEmpty()) paragraphs.add(p.trim());
            }
        }

        List<Task<String>> tasks = new ArrayList<>();
        tasks.add(translator.translate(meal.strMeal));
        for (String line : ingLines) {
            // bỏ "• " ở đầu, dịch xong thêm lại
            tasks.add(translator.translate(line.startsWith("• ") ? line.substring(2) : line));
        }
        for (String p : paragraphs) {
            tasks.add(translator.translate(p));
        }

        Tasks.<String>whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    if (isFinishing() || isDestroyed()) return;
                    int n = ingLines.size();

                    viName = results.get(0);

                    StringBuilder ing = new StringBuilder();
                    for (int i = 0; i < n; i++) {
                        if (i > 0) ing.append("\n");
                        ing.append("• ").append(results.get(1 + i));
                    }
                    viIngredients = ing.toString();

                    StringBuilder ins = new StringBuilder();
                    for (int i = 0; i < paragraphs.size(); i++) {
                        if (i > 0) ins.append("\n\n");
                        ins.append(results.get(1 + n + i));
                    }
                    viInstructions = ins.toString();
                    cache.saveTranslation(meal.idMeal, viName, viIngredients, viInstructions);

                    tvTranslateStatus.setVisibility(View.GONE);
                    applyLanguage(true);
                })
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    tvTranslateStatus.setText("Dịch bị lỗi. Đang hiện bản gốc.");
                });
    }

    private void applyLanguage(boolean vietnamese) {
        showVietnamese = vietnamese;
        tvName.setText(vietnamese ? viName : enName);
        tvIngredients.setText(vietnamese ? viIngredients : enIngredients);
        tvInstructions.setText(vietnamese ? viInstructions : enInstructions);
        btnLang.setText(vietnamese ? "🌐 Xem bản gốc (English)" : "🌐 Xem bản tiếng Việt");
        btnLang.setVisibility(View.VISIBLE);
    }

    private void updateHeart(boolean isFav) {
        btnFavorite.setText(isFav ? "♥" : "♡");
        btnFavorite.setTextColor(isFav ? 0xFFE53935 : 0xFF000000);
    }
}