package com.example.cookingingredients.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.cookingingredients.data.remote.MealSummary;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {

    private static final String PREF_NAME = "favorites";
    private static final String KEY_LIST = "list";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public FavoriteManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public List<MealSummary> getAll() {
        String json = prefs.getString(KEY_LIST, null);
        if (json == null) return new ArrayList<>();
        List<MealSummary> list = gson.fromJson(
                json, new TypeToken<List<MealSummary>>() {}.getType());
        return list == null ? new ArrayList<>() : list;
    }

    public boolean isFavorite(String id) {
        for (MealSummary m : getAll()) {
            if (m.getIdMeal().equals(id)) return true;
        }
        return false;
    }

    // Trả về true nếu sau khi bấm món đang là yêu thích
    public boolean toggle(MealSummary meal) {
        List<MealSummary> list = getAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIdMeal().equals(meal.getIdMeal())) {
                list.remove(i);
                save(list);
                return false;
            }
        }
        list.add(0, meal);
        save(list);
        return true;
    }

    private void save(List<MealSummary> list) {
        prefs.edit().putString(KEY_LIST, gson.toJson(list)).apply();
    }
}