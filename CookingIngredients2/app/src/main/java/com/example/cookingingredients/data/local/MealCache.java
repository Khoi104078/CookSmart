package com.example.cookingingredients.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.cookingingredients.data.remote.MealDetail;
import com.google.gson.Gson;

public class MealCache {

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public MealCache(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences("meal_cache", Context.MODE_PRIVATE);
    }

    public void saveDetail(MealDetail meal) {
        prefs.edit().putString("detail_" + meal.idMeal, gson.toJson(meal)).apply();
    }

    public MealDetail getDetail(String mealId) {
        String json = prefs.getString("detail_" + mealId, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, MealDetail.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void saveTranslation(String mealId, String name, String ingredients, String instructions) {
        prefs.edit()
                .putString("vi_name_" + mealId, name)
                .putString("vi_ing_" + mealId, ingredients)
                .putString("vi_ins_" + mealId, instructions)
                .apply();
    }

    // Trả về {tên, nguyên liệu, các bước} hoặc null nếu chưa có bản dịch
    public String[] getTranslation(String mealId) {
        String name = prefs.getString("vi_name_" + mealId, null);
        String ing = prefs.getString("vi_ing_" + mealId, null);
        String ins = prefs.getString("vi_ins_" + mealId, null);
        if (name == null || ing == null || ins == null) return null;
        return new String[]{name, ing, ins};
    }
}