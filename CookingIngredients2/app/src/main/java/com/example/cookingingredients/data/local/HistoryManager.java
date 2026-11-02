package com.example.cookingingredients.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static final String PREF_NAME = "history";
    private static final String KEY_LIST = "list";
    private static final int MAX_ITEMS = 20;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public HistoryManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Mỗi phần tử là 1 lần tìm: danh sách tên API, ví dụ ["chicken","garlic"]
    public List<List<String>> getAll() {
        String json = prefs.getString(KEY_LIST, null);
        if (json == null) return new ArrayList<>();
        List<List<String>> list = gson.fromJson(
                json, new TypeToken<List<List<String>>>() {}.getType());
        return list == null ? new ArrayList<>() : list;
    }

    public void add(List<String> apiNames) {
        List<List<String>> list = getAll();
        // Nếu đã có lần tìm y hệt thì xóa bản cũ để đưa lên đầu
        list.removeIf(item -> item.equals(apiNames));
        list.add(0, new ArrayList<>(apiNames));
        while (list.size() > MAX_ITEMS) list.remove(list.size() - 1);
        save(list);
    }

    public void clear() {
        prefs.edit().remove(KEY_LIST).apply();
    }

    private void save(List<List<String>> list) {
        prefs.edit().putString(KEY_LIST, gson.toJson(list)).apply();
    }
}